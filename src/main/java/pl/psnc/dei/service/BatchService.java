package pl.psnc.dei.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.dei.controllers.requests.CreateImportFromDatasetRequest;
import pl.psnc.dei.controllers.requests.UploadDatasetRequest;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.*;
import pl.psnc.dei.model.DAO.DatasetsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.ImportNameCreatorUtil;

import javax.transaction.Transactional;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

@Service
@Transactional
public class BatchService {
	private final static Logger log = LoggerFactory.getLogger(BatchService.class);

	private static final String CHANGING_DIMENSION_MSG = "Changing %s from %d to %d";

	private static final String UPDATING_DIMENSIONS_MSG = "Updating dimensions for %s";

	private final RecordsRepository recordsRepository;

	private final DatasetsRepository datasetsReposotory;

	private final ProjectsRepository projectsRepository;

	private final EuropeanaSearchService europeanaSearchService;

	private final ImportPackageService importService;

	public BatchService(RecordsRepository recordsRepository, DatasetsRepository datasetsReposotory, ProjectsRepository projectsRepository, EuropeanaSearchService europeanaSearchService, ImportPackageService importService) {
		this.recordsRepository = recordsRepository;
		this.datasetsReposotory = datasetsReposotory;
		this.projectsRepository = projectsRepository;
		this.europeanaSearchService = europeanaSearchService;
		this.importService = importService;
	}

	/**
	 * Add multiple records. If dataset is missing then it is added.
	 * @param projectId id of project to which records should belong
	 * @param datasetId (optional) dataset to which records should belong
	 * @param recordIds record identifier for aggregator
	 * @return candaidates, that are not in import
	 * @throws NotFoundException if some information is missing
	 */
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

		recordIds.stream()
				.filter(r -> r != null && !r.isEmpty())
				.forEach(recordId -> {
					Record record = recordsRepository.findByIdentifierAndProjectAndDataset(recordId, project, dataset);
					if (record == null) {
						// record does not belongs to some project
						record = recordsRepository.findByIdentifierAndProject(recordId, project);
						if (record == null) {
							// record does not exist
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
							// record exist but have no dataset assigned
							record.setDataset(dataset);
							recordsRepository.save(record);
							if (record.getAnImport() == null) {
								candidates.add(record);
							}
						}
					} else if (record.getAnImport() == null) {
						// project exist and have data set assigned
						candidates.add(record);
					}
				});
		return candidates;
	}

	/**
	 * fetch title of record from europeana
	 * @param recordId european record id
	 * @return title
	 */
	private String getTitle(String recordId) {
		JsonObject jsonObject;
		try {
			 jsonObject = europeanaSearchService.retrieveRecordAndConvertToJsonLd(recordId);
		} catch (RuntimeException e) {
			throw new RuntimeException("Error fetching data for record " + recordId + ". " + e.getMessage());
		}
		if (jsonObject != null) {
			Optional<JsonObject> title = jsonObject
					.get("@graph")
					.getAsArray()
					.stream()
					.map(JsonValue::getAsObject)
					.filter(obj -> obj.get("dc:title") != null)
					.findFirst();
			if (title.isPresent()) {
				JsonValue dcTitle = title.get().getAsObject().get("dc:title");
				if (dcTitle.isObject()) {
					return dcTitle.getAsObject().get("@value").getAsString().value();
				}
				if (dcTitle.isString()) {
					return dcTitle.getAsString().value();
				}
				log.warn("Cannot retrieve title for record " + recordId);
			}
		}
		return recordId;
	}

	public List<Set<String>> splitImport(File file) throws IOException {
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

	/**
	 * Reads file content and populate map with it
	 * @param file file to read
	 * @return map populated with file contents
	 * @throws IOException on file read error
	 */
	private Map<String, List<String>> readRecordsFromFile(File file) throws IOException {
		if (file.length() == 0) {
			throw new IllegalArgumentException("Empty file");
		}

		Map<String, List<String>> records = new HashMap<>();

		BufferedReader reader = new BufferedReader(new FileReader(file));
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
		Set<String> images = new HashSet<>();

		Map<String, Map<String, String>> dimensions = readDimensionsFromFile(file);

		dimensions.forEach((key, value) -> recordsRepository.findByIdentifier(key).ifPresent(record -> {
			Map<String, List<String>> manifest = fixDimensions(key, value, record.getIiifManifest());
			if (!manifest.isEmpty()) {
				if (fix) {
					record.setIiifManifest(manifest.keySet().iterator().next());
					recordsRepository.save(record);
				}
				images.addAll(manifest.values().iterator().next());
			}
		}));
		return images;
	}

	private Map<String, List<String>> fixDimensions(String identifier, Map<String, String> dimensions, String iiifManifest) {
		List<String> updatedImages = new ArrayList<>();
		JsonObject jsonObject = JSON.parse(iiifManifest);
		JsonArray canvas = jsonObject.get("sequences").getAsArray().get(0).getAsObject().get("canvases").getAsArray();
		canvas.stream().iterator().forEachRemaining(canva -> {
			final JsonValue label = canva.getAsObject().get("label");
			String key = label.getAsString().value();
			if (key.contains(identifier)) {
				key = key.substring(key.indexOf(identifier));
				log.info(String.format(UPDATING_DIMENSIONS_MSG, key));
				String[] size = dimensions.get(key).split("x");
				int width = Integer.parseInt(size[0].trim());
				int height = Integer.parseInt(size[1].trim());
				boolean updated = updateDimension(canva, width, "width");
				updated |= updateDimension(canva, height, "height");
				if (updated) {
					updatedImages.add(key);
				}
				JsonArray images = canva.getAsObject().get("images").getAsArray();
				images.stream().iterator().forEachRemaining(image -> {
					JsonValue on = image.getAsObject().get("on");
					if (on.getAsString().value().endsWith(label.getAsString().value())) {
						log.info(String.format(UPDATING_DIMENSIONS_MSG, on.getAsString().value()));
						updateDimension(image.getAsObject().get("resource"), width, "width");
						updateDimension(image.getAsObject().get("resource"), height, "height");
					}
				});
			}
		});
		if (updatedImages.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, List<String>> updatedManifest = new HashMap<>();
		updatedManifest.put(jsonObject.toString(), updatedImages);
		return updatedManifest;
	}

	private boolean updateDimension(JsonValue parent, int dimension, String label) {
		JsonValue dimensionObject = parent.getAsObject().get(label);
		int current = dimensionObject.getAsNumber().value().intValue();
		if (current != dimension) {
			log.info(String.format(CHANGING_DIMENSION_MSG, label, current, dimension));
			parent.getAsObject().put(label, dimension);
			return true;
		}
		return false;
	}

	private String extractIdentifier(String key) {
		int index = key.indexOf('/', 1);
		if (index != -1) {
			index = key.indexOf('/', index + 1);
		}
		if (index != -1) {
			return key.substring(0, index);
		}
		return null;
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
				if (identifier == null) {
					log.warn("Could not extract identifier from " + parts[0]);
				} else {
					allDimensions.computeIfAbsent(identifier, s -> new HashMap<>()).put(parts[0].trim(), parts[1].trim());
				}
			}
		}
		return allDimensions;
	}

	public List<Dataset> getProjectDataset(Project project) {
		Project currentProject = projectsRepository.findByProjectId(project.getProjectId());
		Hibernate.initialize(currentProject.getDatasets());
		return currentProject.getDatasets();
	}

	/**
	 * Gets all of the records for specified Europeana dataset (by dataset id)
	 * and upload them to the local database
	 * @param request which contains processing settings
	 * @return set of records for whole Europeana dataset (except excluded records or duplications)
	 * @throws IllegalArgumentException when missing required params
	 * @throws NotFoundException when cannot found data in local database
	 */
	public Set<Record> uploadDataset(UploadDatasetRequest request)
			throws IllegalArgumentException, NotFoundException {
		String projectName = request.getProjectName();
		String datasetId = request.getDatasetId();
		if (projectName == null || datasetId == null) {
			throw new IllegalArgumentException();
		}
		Set<String> recordsIds = europeanaSearchService.getAllDatasetRecords(datasetId);
		Set<String> excludedFiltered = filterExcluded(recordsIds, request.getExcludedRecords());
		Set<String> numberLimited;
		if (request.getLimit() > 0) {
			int recordsToRetrieve = Math.min(excludedFiltered.size(), request.getLimit());
			numberLimited = excludedFiltered.stream()
					.limit(recordsToRetrieve)
					.collect(Collectors.toSet());
		} else {
			numberLimited = excludedFiltered;
		}
		return uploadRecordsToProject(projectName, request.getDataset(), numberLimited);
    }

	/**
	 * Creates import for all records from Europena dataset (by dataset id).
	 * except excluded records or duplications
	 * @param request which contains processing settings
	 * @return created imports
	 * @throws IllegalArgumentException when missing required params
	 * @throws NotFoundException when cannot found data in local database
	 */
    public List<Import> createImportsFromDataset(CreateImportFromDatasetRequest request)
			throws IllegalArgumentException, NotFoundException {
		Set<Record> uploadedDataset = uploadDataset(request);
		String projectId = uploadedDataset.iterator().next().getProject().getProjectId();
		int importSize = request.getImportSize();
		List<Set<Record>> splitRecords;
		boolean splitImport = importSize > 0;
		if (splitImport) {
			splitRecords = new ArrayList<>();
			Iterables.partition(uploadedDataset, importSize).forEach(part -> splitRecords.add(Set.copyOf(part)));
		} else {
			splitRecords = Collections.singletonList(uploadedDataset);
		}
		AtomicInteger index = new AtomicInteger(0);
		return splitRecords.stream()
				.map(records -> {
					String importName = generateSplitImportName(request.getImportName(), request.getProjectName(), index.getAndIncrement(), splitImport);
					return importService.createImport(importName, projectId, records);
				}).collect(Collectors.toList());
	}

	private String generateSplitImportName(String importName, String projectName, int counter, boolean doSplitImport) {
		String importNameBase = StringUtils.isNotBlank(importName) ? importName : ImportNameCreatorUtil.generateImportName(projectName);
		if (doSplitImport) {
			return importNameBase.concat(String.format("_%d", counter));
		}
		return importNameBase;
	}

    private Set<String> filterExcluded(Set<String> allRecords, Set<String> toExclude) {
		if (CollectionUtils.isEmpty(toExclude)) {
			return allRecords;
		}
        allRecords.removeAll(toExclude);
        return allRecords;
    }

	/**
	 * Saves records to DB, and calculate difference of saved and provided ones, as some of them can
	 * be omitted during persiting process
	 * @param projectName name of project to which data should be saved
	 * @param datasetName name of dataset from which data comes
	 * @param recordsIds id of data to save
	 * @return saved records
	 * @throws NotFoundException
	 */
	public Set<Record> uploadRecordsToProject(String projectName,
											  String datasetName,
											  Set<String> recordsIds)
			throws NotFoundException {
		String datasetId = null;
		if (datasetName != null) {
			Optional<Dataset> optionalDataset = this.datasetsReposotory.findByName(datasetName);
			if (optionalDataset.isEmpty()) {
				throw new NotFoundException("Dataset with name: " + datasetName + " not found");
			}
			datasetId = optionalDataset.get().getDatasetId();
		}
		Set<Record> records = this.uploadRecords(projectName, datasetId, recordsIds);
		if (records.size() < recordsIds.size()) {
			String difference = records
					.stream()
					.map(Record::getIdentifier)
					.filter(identifier -> !recordsIds.contains(identifier)).collect(Collectors.joining(","));
			if (difference.isEmpty()) {
				difference = String.join(",", recordsIds);
			}
			log.warn("Following records will not be added to the import: {}", difference);
		}
		return records;
	}

	public List<Import> makeComplexImport(MultipartFile file, String name, String projectName, String datasetName) throws IOException, NotFoundException {
		File saved = null;
		try {
			saved = File.createTempFile("tmp", ".csv");
			file.transferTo(saved);
			return this.makeComplexImport(saved, name, projectName, datasetName);
		} finally {
			saved.delete();
		}
	}

	public List<Import> makeComplexImport(InputStream inputStream, String name, String projectName, String datasetName) throws IOException, NotFoundException {
		File saved = null;
		try {
			saved = File.createTempFile("tmp", ".csv");
			OutputStream os = new FileOutputStream(saved);
			os.write(inputStream.readAllBytes());
			return this.makeComplexImport(saved, name, projectName, datasetName);
		} finally {
			saved.delete();
		}
	}

	public List<Import> makeComplexImport(File file, String name, String projectName, String datasetName) throws IOException, NotFoundException {
		List<Set<String>> records = this.splitImport(file);
		List<Import> imports = new ArrayList<>();
		int counter = 0;
		String importName;

		for (Set<String> identifiers : records) {
			Set<Record> uploadedRecords = uploadRecordsToProject(projectName, datasetName, identifiers);
			if (!uploadedRecords.isEmpty()) {
				if (StringUtils.isBlank(name)) {
					importName = name;
					} else {
						importName = name + "_" + counter++;
					}
					imports.add(importService.createImport(importName, uploadedRecords.iterator().next().getProject().getProjectId(), uploadedRecords));
				}
			}
		return imports;
	}
}
