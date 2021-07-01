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
