package pl.psnc.dei.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.dei.controllers.requests.CreateImportFromDatasetRequest;
import pl.psnc.dei.controllers.requests.UploadDatasetRequest;
import pl.psnc.dei.controllers.responses.CallToActionResponse;
import pl.psnc.dei.controllers.responses.ManifestRecreationResponse;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.service.ImportPackageService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

	private final Logger logger = LoggerFactory.getLogger(BatchController.class);

	private final BatchService batchService;

	private final ImportPackageService importService;

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
			Set<Record> records = this.batchService.uploadRecordsToProject(projectName, datasetName, recordsIds);
			if (records.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(importService.createImport(name, records.iterator().next().getProject().getProjectId(), records), HttpStatus.OK);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
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
		try {
			return ResponseEntity.ok(this.batchService.makeComplexImport(file, name, projectName, datasetName));
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(path = "/fix-dimensions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Set<String>> fixDimensions(@RequestParam(required = false, defaultValue = "false") boolean fix,
													 @RequestBody @RequestParam("file") MultipartFile file) throws IOException {
		Set<String> fileToDimension = batchService.fixDimensions(fix, file);
		return ResponseEntity.ok(fileToDimension);
	}

	@PostMapping(path = "/datasets/records", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Set<Record>> datasetUpload(@RequestBody UploadDatasetRequest request) {
		try {
			Set<Record> records = batchService.uploadDataset(request);
			if (records.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return ResponseEntity.ok(records);
		} catch (NotFoundException nfe) {
			logger.error(nfe.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException iae) {
			logger.error(iae.getMessage());
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@PostMapping(path = "/datasets/imports", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Import>> createImportFromDataset(@RequestBody CreateImportFromDatasetRequest request) {
		try {
			List<Import> imports = batchService.createImportsFromDataset(request);
			if (imports.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return ResponseEntity.ok(imports);
		} catch (NotFoundException nfe) {
			logger.error(nfe.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException iae) {
			logger.error(iae.getMessage());
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@PostMapping(path = "/fix-manifests", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ManifestRecreationResponse> fixManifests() {
		logger.info("Fix all manifests started");
		ManifestRecreationResponse response = batchService.fixManifests();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(path = "/fix-manifest", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ManifestRecreationResponse> fixManifest(@RequestParam String recordId) {
		logger.info("Fix manifest for record {} started", recordId);
		ManifestRecreationResponse response = null;
		try {
			response = batchService.fixManifest(recordId);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(path = "/calltoaction", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CallToActionResponse> callToAction(@RequestParam(required = false, defaultValue = "false") boolean validateManifest,
			@RequestParam(required = false, defaultValue = "true") boolean simulate, @RequestParam(required = false, defaultValue = "true") boolean includeRecords,
			@RequestBody Set<String> recordsIds) {
		CallToActionResponse response = batchService.callToAction(validateManifest, simulate, includeRecords, recordsIds);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
