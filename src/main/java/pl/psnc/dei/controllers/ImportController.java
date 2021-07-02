package pl.psnc.dei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.ImportPackageService;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class ImportController {

	private ImportPackageService importService;

	@Autowired
	public ImportController(ImportPackageService importService) {
		this.importService = importService;
	}

	/**
	 * creates import (single?)
	 * @param projectId id of project from which records was taken
	 * @param name name of new import
	 * @param records records to submit into import
	 * @return REST response with created import
	 */
	@PostMapping("/import")
	public ResponseEntity<Import> createImport(@RequestParam(value = "projectId") String projectId, @RequestParam(value = "name", required = false) String name, @RequestBody Set<Record> records) {
		return new ResponseEntity<>(importService.createImport(name, projectId, records), HttpStatus.OK);
	}

	/**
	 * Lists candidating records for new import
	 * @param projectId project from which records should be shown
	 * @param datasetId optional filtering of records to only match given dataset - data set is filed of record then
	 * @return REST response with all matching candidates
	 */
	@GetMapping("/import/candidates")
	public ResponseEntity<Set<Record>> getCandidates(@RequestParam(value = "projectId") String projectId, @RequestParam(value = "datasetId", required = false) String datasetId) {
		return new ResponseEntity<>(importService.getCandidates(projectId, datasetId), HttpStatus.OK);
	}

	/**
	 * Check status of an import
	 * @param importName import name of which status ought to be checked
	 * @return REST response with status
	 */
	@GetMapping("/import/status")
	public ResponseEntity getImportReport(@RequestParam(value = "importName") String importName) {
		try {
			return new ResponseEntity<>(importService.getStatusWithFailure(importName), HttpStatus.OK);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Send import to transcription platform
	 * @param importName import name to be send
	 */
	@PostMapping("/import/send")
	public ResponseEntity sendImport(@RequestParam(value = "importName") String importName) {
		try {
			importService.sendExistingImport(importName);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PutMapping("/import")
	public ResponseEntity addRecordsToAlreadyCreatedImport(@RequestParam(value = "importName") String importName, @RequestBody Set<Record> records) {
		try {
			return new ResponseEntity<>(importService.addRecordsToImport(importName, records), HttpStatus.CREATED);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
