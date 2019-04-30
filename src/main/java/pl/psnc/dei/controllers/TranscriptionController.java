package pl.psnc.dei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.queue.task.UpdateTask;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.RecordIdValidator;

@RestController
@RequestMapping("/api/transcription")
public class TranscriptionController {

	private TranscriptionPlatformService transcriptionPlatformService;

	private TasksQueueService tasksQueueService;

	@Autowired
	public TranscriptionController(TranscriptionPlatformService transcriptionPlatformService, TasksQueueService tasksQueueService) {
		this.transcriptionPlatformService = transcriptionPlatformService;
		this.tasksQueueService = tasksQueueService;
	}

	@PostMapping
	public ResponseEntity transcriptionReady(@RequestParam(value = "recordId") String recordId) {

		if (!RecordIdValidator.validate(recordId)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			transcriptionPlatformService.createNewTranscribeTask(recordId);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PutMapping
	public ResponseEntity updateTranscription(
			@RequestParam("annotationId") String annotationId,
			@RequestParam("recordId") String recordId,
			@RequestParam("transcriptionId") String transcriptionId) {

		if (!RecordIdValidator.validate(recordId)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			tasksQueueService.addTaskToQueue(new UpdateTask(recordId, annotationId, transcriptionId));
		} catch (NotFoundException e) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity(HttpStatus.OK);
	}

}
