package pl.psnc.dei.controllers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.service.ImportPackageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

	private Logger logger = LoggerFactory.getLogger(BatchController.class);

	private BatchService batchService;

	private ImportPackageService importService;

	public BatchController(BatchService batchService, ImportPackageService importService) {
		this.batchService = batchService;
		this.importService = importService;
	}

	/**
	 * Upload records to DB. If no dataset provided null assigned. If record exist no duplicate made
	 * @param projectName project to which records belong
	 * @param datasetName dataset from which records orginate
	 * @param recordsIds ids of records to save
	 * @return HTTP response code
	 */
	@PostMapping("/records")
	public ResponseEntity<String> uploadRecords(@RequestParam(value = "projectName") String projectName,
												@RequestParam(value = "datasetName", required = false) String datasetName,
												@RequestBody Set<String> recordsIds) {
		try {
			batchService.uploadRecords(projectName, datasetName, recordsIds);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Saves records to DB and create new import out of them
	 * @param projectName name of project to which data should be saved
	 * @param datasetName dataset name from which data comes
	 * @param name name of new import
	 * @param recordsIds ids of records to save
	 * @return HTTP response code
	 */
	@PostMapping("/imports")
	public ResponseEntity<Import> uploadRecordsAndCreateImport(@RequestParam(value = "projectName") String projectName,
															   @RequestParam(value = "datasetName", required = false) String datasetName,
															   @RequestParam(value = "name", required = false) String name,
															   @RequestBody Set<String> recordsIds) {
		try {
			Set<Record> records = uploadRecordsToProject(projectName, datasetName, recordsIds);
			if (records.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(importService.createImport(name, records.iterator().next().getProject().getProjectId(), records), HttpStatus.OK);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
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
	private Set<Record> uploadRecordsToProject(String projectName,
											   String datasetName,
											   Set<String> recordsIds)
			throws NotFoundException {
		Set<Record> records = batchService.uploadRecords(projectName, datasetName, recordsIds);
		if (records.size() < recordsIds.size()) {
			String difference = records
					.stream()
					.filter(record -> !recordsIds.contains(record.getIdentifier()))
					.map(Record::getIdentifier).collect(Collectors.joining(","));
			if (difference.isEmpty()) {
				difference = recordsIds.stream().collect(Collectors.joining(","));
			}
			logger.warn("Following records will not be added to the import: {}", difference);
		}
		return records;
	}

	/**
	 * Uploads records to DB and create imports, based on multipart file and single/complex record structure of
	 * file
	 * @param projectName name of project to which data should be saved
	 * @param datasetName name of dataset from which dataset comes
	 * @param name name of import
	 * @param file file from which records should be taken
	 * @return HTTP Response code
	 * @throws IOException
	 */
	@PostMapping(path = "/complex-imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Import>> splitImport(@RequestParam(value = "projectName") String projectName,
											  		@RequestParam(value = "datasetName", required = false) String datasetName,
											  		@RequestParam(value = "name", required = false) String name,
											  		@RequestBody @RequestParam("file") MultipartFile file) throws IOException {
		List<Set<String>> records = batchService.splitImport(file);
		List<Import> imports = new ArrayList<>();

		try {
			int counter = 0;
			String importName;

			for(Set<String> identifiers : records) {
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
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(imports);
	}

	@PostMapping(path = "/fix-dimensions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Set<String>> fixDimensions(@RequestParam(required = false, defaultValue = "false") boolean fix,
													  @RequestBody @RequestParam("file") MultipartFile file) throws IOException {
		Set<String> fileToDimension = batchService.fixDimensions(fix, file);
		return ResponseEntity.ok(fileToDimension);
	}
}
