package pl.psnc.dei.controllers;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.queue.task.TasksFactory;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.EuropeanaRecordIdValidator;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/transcription")
public class TranscriptionController {

	private Logger logger = LoggerFactory.getLogger(TranscriptionController.class);

	private TranscriptionPlatformService tps;

	private TasksQueueService tqs;

	private TasksFactory tasksFactory;

	@Autowired
	public TranscriptionController(TranscriptionPlatformService tps, TasksQueueService tqs, TasksFactory tasksFactory) {
		this.tps = tps;
		this.tqs = tqs;
		this.tasksFactory = tasksFactory;
	}

	@PostMapping
	public ResponseEntity transcriptionReady(@RequestParam(value = "recordId") String recordId) {

		logger.info("Transcription ready {}", recordId);

		if (!EuropeanaRecordIdValidator.validate(recordId)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			tps.createNewEnrichTask(recordId);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/batch")
	public ResponseEntity<String> transcriptionsReady(@RequestBody Set<String> recordsIds) {
		if (!recordsIds.stream().allMatch(EuropeanaRecordIdValidator::validate)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Set<String> notFound = new HashSet<>();

		for (String recordId : recordsIds) {
			logger.info("Creating enrich task for record {}", recordId);
			try {
				tps.createNewEnrichTask(recordId);
			} catch (NotFoundException e) {
				notFound.add(e.getMessage());
			}
		}
		if (!notFound.isEmpty()) {
			return new ResponseEntity<>(String.join(",", notFound), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping
	public ResponseEntity updateTranscription(
			@RequestParam("annotationId") String annotationId,
			@RequestParam("recordId") String recordId,
			@RequestParam("transcriptionId") String transcriptionId) {

		logger.info("Transcription updated {}", recordId);

		if (!EuropeanaRecordIdValidator.validate(recordId)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			tqs.addTaskToQueue(tasksFactory.getNewUpdateTask(recordId, annotationId, transcriptionId));
		} catch (NotFoundException e) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity(HttpStatus.OK);
	}

	@GetMapping(value = "/iiif/manifest", produces = "application/json")
	public ResponseEntity getManifest(@RequestParam("recordId") String recordId) {
		try {
			JsonObject manifest = tps.getManifest(recordId);
			return ResponseEntity.status(HttpStatus.OK).body(manifest.toString());
		} catch (NotFoundException e) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}

}
