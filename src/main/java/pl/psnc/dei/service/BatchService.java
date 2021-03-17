package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.DatasetsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

@Service
@Transactional
public class BatchService {
	private final static Logger log = LoggerFactory.getLogger(BatchService.class);

	private final RecordsRepository recordsRepository;

	private final DatasetsRepository datasetsReposotory;

	private final ProjectsRepository projectsRepository;

	private final EuropeanaSearchService europeanaSearchService;

	public BatchService(RecordsRepository recordsRepository, DatasetsRepository datasetsReposotory, ProjectsRepository projectsRepository, EuropeanaSearchService europeanaSearchService) {
		this.recordsRepository = recordsRepository;
		this.datasetsReposotory = datasetsReposotory;
		this.projectsRepository = projectsRepository;
		this.europeanaSearchService = europeanaSearchService;
	}


	public Set<Record> uploadRecords(String projectId, String datasetId, Set<String> recordIds) throws NotFoundException {
		Project project = projectsRepository.findByName(projectId);
		if (project == null) {
			throw new NotFoundException("Project " + projectId + " not found!");
		}
		Dataset dataset;
		if (datasetId != null) {
			dataset = datasetsReposotory.findDatasetByDatasetId(datasetId);
			if (dataset == null) {
				throw new NotFoundException("Dataset " + datasetId + " not found!");
			}
		} else {
			dataset = null;
		}

		Set<Record> candidates = new HashSet<>();

		recordIds.stream().filter(r -> r != null && !r.isEmpty()).forEach(recordId -> {
			Record record = recordsRepository.findByIdentifierAndProjectAndDataset(recordId, project, dataset);
			if (record == null) {
				record = recordsRepository.findByIdentifierAndProject(recordId, project);
				if (record == null) {
					// new record - get necessary information from Europeana
					Record newRecord = new Record();
					newRecord.setIdentifier(recordId);
					newRecord.setTitle(getTitle(recordId));
					newRecord.setProject(project);
					newRecord.setDataset(dataset);
					newRecord.setAggregator(Aggregator.EUROPEANA);
					recordsRepository.save(newRecord);
					candidates.add(newRecord);
				} else {
					record.setDataset(dataset);
					recordsRepository.save(record);
					if (record.getAnImport() == null) {
						candidates.add(record);
					}
				}
			} else if (record.getAnImport() == null) {
				candidates.add(record);
			}
		});
		return candidates;
	}

	private String getTitle(String recordId) {
		JsonObject jsonObject = europeanaSearchService.retrieveRecordAndConvertToJsonLd(recordId);
		if (jsonObject != null) {
			Optional<JsonObject> title = jsonObject
					.get("@graph")
					.getAsArray()
					.stream()
					.map(JsonValue::getAsObject)
					.filter(obj -> obj.get("dc:title") != null)
					.findFirst();
			if (title.isPresent()) {
				return title.get().getAsObject().get("dc:title").getAsString().value();
			}
		}
		return recordId;
	}

	public List<Set<String>> splitImport(MultipartFile file) throws IOException {
		Map<String, List<String>> records = readRecordsFromFile(file);
		List<Set<String>> result = new ArrayList<>();

		final List<String> singleRecords = new ArrayList<>();
		final List<String> complexRecords = new ArrayList<>();

		records
				.values().forEach(identifiers -> {
					if (identifiers.size() == 1) {
						if (singleRecords.size() == 1000) {
							result.add(new HashSet<>(singleRecords));
							singleRecords.clear();
						}
						identifiers.stream().map(s -> s.replace(EUROPEANA_ITEM_URL, "")).forEach(singleRecords::add);
					} else {
						if (complexRecords.size() + identifiers.size() >= 1000) {
							result.add(new HashSet<>(complexRecords));
							complexRecords.clear();
						}
						identifiers.stream().map(s -> s.replace(EUROPEANA_ITEM_URL, "")).forEach(complexRecords::add);
					}
		});
		if (!singleRecords.isEmpty()) {
			result.add(new HashSet<>(singleRecords));
		}
		if (!complexRecords.isEmpty()) {
			result.add(new HashSet<>(complexRecords));
		}

		return result;
	}

	private Map<String, List<String>> readRecordsFromFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Empty file");
		}

		Map<String, List<String>> records = new HashMap<>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
		while (reader.ready()) {
			String line = reader.readLine();
			String[] parts = line.split(",");
			if (parts.length >= 3) {
				records.computeIfAbsent(parts[0], k -> new ArrayList<>());
				records.get(parts[0]).add(parts[2]);
			}
		}
		return records;
	}

	public Set<String> fixDimensions(boolean fix, MultipartFile file) throws IOException {
		Set<String> manifests = new HashSet<>();

		Map<String, Map<String, String>> dimensions = readDimensionsFromFile(file);

		dimensions.forEach((key, value) -> {
			recordsRepository.findByIdentifier(key).ifPresent(record -> {
				String manifest = fixDimensions(key, value, record.getIiifManifest());
				if (fix) {
					record.setIiifManifest(manifest);
					recordsRepository.save(record);
				}
				manifests.add(manifest);
			});
		});
		return manifests;
	}

	private String fixDimensions(String identifier, Map<String, String> dimensions, String iiifManifest) {
		JsonObject jsonObject = JSON.parse(iiifManifest);
		JsonArray canvas = jsonObject.get("sequences").getAsArray().get(0).getAsObject().get("canvases").getAsArray();
		canvas.stream().iterator().forEachRemaining(canva -> {
			final JsonValue label = canva.getAsObject().get("label");
			String key = label.getAsString().value();
			if (key.contains(identifier)) {
				key = key.substring(key.indexOf(identifier));
				String[] size = dimensions.get(key).split("x");
				int width = Integer.parseInt(size[0].trim());
				int height = Integer.parseInt(size[1].trim());
				log.debug(String.format("Updating width for %s", key));
				updateDimension(canva, width, "width");
				log.debug(String.format("Updating height for %s", key));
				updateDimension(canva, height, "height");
				JsonArray images = canva.getAsObject().get("images").getAsArray();
				images.stream().iterator().forEachRemaining(image -> {
					JsonValue on = image.getAsObject().get("on");
					if (on.getAsString().value().endsWith(label.getAsString().value())) {
						log.debug(String.format("Updating width for %s", on.getAsString().value()));
						updateDimension(image.getAsObject().get("resource"), width, "width");
						log.debug(String.format("Updating height for %s", on.getAsString().value()));
						updateDimension(image.getAsObject().get("resource"), height, "height");
					}
				});
			}
		});
		return jsonObject.toString();
	}

	private void updateDimension(JsonValue parent, int dimension, String label) {
		JsonValue dimensionObject = parent.getAsObject().get(label);
		if (dimensionObject.getAsNumber().value().intValue() != dimension) {
			log.debug(String.format("Changing %s from %d to %d", label, dimensionObject.getAsNumber().value().intValue(), dimension));
			parent.getAsObject().put(label, dimension);
		}
	}

	private String extractIdentifier(String key) {
		int index = key.indexOf('/', 1);
		if (index != -1) {
			index = key.indexOf('/', index + 1);
		}
		if (index != -1) {
			return key.substring(0, index);
		}
		return key;
	}

	private Map<String, Map<String, String>> readDimensionsFromFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Empty file");
		}
		Map<String, Map<String, String>> allDimensions = new HashMap<>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
		while (reader.ready()) {
			String line = reader.readLine();
			String[] parts = line.split(":");
			if (parts.length == 2) {
				String identifier = extractIdentifier(parts[0]);
				allDimensions.computeIfAbsent(identifier, s -> new HashMap<>()).put(parts[0].trim(), parts[1].trim());
			}
		}
		return allDimensions;
	}
}
