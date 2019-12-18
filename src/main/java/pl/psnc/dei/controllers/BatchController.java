package pl.psnc.dei.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.service.ImportPackageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
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
			if (records.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(importService.createImport(name, records.iterator().next().getProject().getProjectId(), records), HttpStatus.OK);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
