package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.exception.TaskCreationException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.TranscriptionPlatformService;

import java.util.ArrayList;
import java.util.List;

public class UpdateTask extends Task {

	@Autowired
	private static TranscriptionPlatformService transcriptionPlatformService;

	@Autowired
	private static EuropeanaRestService europeanaRestService;

	/**
	 * It is possible that there will be more than 1 transcription update pending, so it has to be list, that situation
	 * can happen only if there will be server crash.
	 */
	private List<Transcription> transcriptions;

	public UpdateTask(Record record) throws TaskCreationException {
		super(record);
		if(record.getTranscriptions().isEmpty()) {
			try {
				queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.NORMAL);
			} catch (NotFoundException e) {
//				Actually, this is impossible
			}
			throw new TaskCreationException("Database inconsistency, update pending task has to have at" +
					" least one transcription! Changing state to normal. Record identifier: " + record.getIdentifier());
		}
		transcriptions = record.getTranscriptions();
		state = TaskState.U_GET_TRANSCRIPTION_FROM_TP;
	}

	public UpdateTask(String recordIdentifier, String annotationId, String transcriptionId) throws NotFoundException {
		super(queueRecordService.getRecord(recordIdentifier));
		Transcription newTranscription = new Transcription(transcriptionId, record, annotationId);
		record.getTranscriptions().add(newTranscription);
		queueRecordService.saveRecord(record);

		transcriptions = new ArrayList<>();
		transcriptions.add(newTranscription);

		queueRecordService.setNewStateForRecord(getRecord().getId(), Record.RecordState.U_PENDING);
		state = TaskState.U_GET_TRANSCRIPTION_FROM_TP;
	}

	@Override
	public void process() throws Exception {
		switch (state) {
			case U_GET_TRANSCRIPTION_FROM_TP:
				for(Transcription t : transcriptions) {
					JsonObject tContent = transcriptionPlatformService.fetchTranscriptionUpdate(t);
//					t.setContent(tContent);
				}
				state = TaskState.U_HANDLE_TRANSCRIPTION;
			case U_HANDLE_TRANSCRIPTION:
				for(Transcription t : transcriptions) {
					europeanaRestService.updateTranscription(t);
					record.getTranscriptions().remove(t);
					queueRecordService.saveRecord(record);
				}

				if(record.getTranscriptions().isEmpty())
					queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.NORMAL);
		}
	}
}
