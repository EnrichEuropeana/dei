package pl.psnc.dei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.RecordIdValidator;

@RestController
@RequestMapping("/api/transcription")
public class TranscriptionController {

	private TranscriptionPlatformService transcriptionPlatformService;

	@Autowired
	public TranscriptionController(TranscriptionPlatformService transcriptionPlatformService) {
		this.transcriptionPlatformService = transcriptionPlatformService;
	}

	@PostMapping
	public ResponseEntity transcriptionReady(@RequestParam(value = "recordId") String recordId){

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
}
