package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.util.Arrays;
import java.util.List;

public class UpdateTask extends Task {

	private static final Logger logger = LoggerFactory.getLogger(UpdateTask.class);

	/**
	 * It is possible that there will be more than 1 transcription update pending, so it has to be list, that situation
	 * can happen only if there will be server crash.
	 */
	private List<Transcription> transcriptions;

	UpdateTask(Record record, QueueRecordService queueRecordService,
			   TranscriptionPlatformService tps, EuropeanaSearchService ess, EuropeanaAnnotationsService eas) {
		super(record, queueRecordService, tps, ess, eas);

		// someone is pushing update to transcription that is not present
		// ignore request
		if (record.getTranscriptions().isEmpty()) {
			try {
				queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.NORMAL);
			} catch (NotFoundException e) {
				throw new AssertionError("Record deleted while being processed, id: " + record.getId()
						+ ", identifier: " + record.getIdentifier(), e);
			}
			logger.error("Database inconsistency, update pending task has to have at" +
					" least one transcription! Changing state to normal. Record identifier: {}", record.getIdentifier());
		}
		transcriptions = record.getTranscriptions();
		state = TaskState.U_GET_TRANSCRIPTION_FROM_TP;
	}

	public UpdateTask(String recordIdentifier, String annotationId, String transcriptionId,
					  QueueRecordService queueRecordService, TranscriptionPlatformService tps, EuropeanaSearchService ess, EuropeanaAnnotationsService eas) throws NotFoundException {
		super(queueRecordService.getRecord(recordIdentifier), queueRecordService, tps, ess, eas);
		// assemble transcription onece more
		Transcription newTranscription = new Transcription(transcriptionId, record, annotationId);
		record.getTranscriptions().add(newTranscription);
		queueRecordService.saveRecord(record);

		transcriptions = Arrays.asList(newTranscription);

		queueRecordService.setNewStateForRecord(getRecord().getId(), Record.RecordState.U_PENDING);
		state = TaskState.U_GET_TRANSCRIPTION_FROM_TP;
	}

	@Override
	public void process() {
		switch (state) {
			case U_GET_TRANSCRIPTION_FROM_TP:
				for (Transcription t : transcriptions) {
					// transcription body are transient so each time we
					// must download it
					JsonObject tContent = tps.fetchTranscriptionUpdate(t);
					t.setTranscriptionContent(tContent);
				}
				state = TaskState.U_HANDLE_TRANSCRIPTION;
			case U_HANDLE_TRANSCRIPTION:
				for (Transcription t : transcriptions) {
					// send updated transcription to europeana
					eas.updateTranscription(t);
					record.getTranscriptions().remove(t);
					queueRecordService.saveRecord(record);
				}

				if (record.getTranscriptions().isEmpty()) {
					try {
						queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.NORMAL);
					} catch (NotFoundException e) {
//						Actually, this is not possible
					}
				}
		}
	}
}
