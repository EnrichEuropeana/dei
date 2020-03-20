package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
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
}
