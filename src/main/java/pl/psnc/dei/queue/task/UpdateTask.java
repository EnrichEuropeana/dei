package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.model.conversion.UpdateTaskContext;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.util.Arrays;
import java.util.List;

public class UpdateTask extends Task {

	private static final Logger logger = LoggerFactory.getLogger(UpdateTask.class);

	/**
	 * It is possible that there will be more than 1 transcription update pending, so it has to be list, that situation
	 * can happen only if there will be server crash.
	 */
	private final List<Transcription> transcriptions;

	private final ContextMediator contextMediator;

	private final UpdateTaskContext context;

	private final QueueRecordService queueRecordService;

	UpdateTask(Record record, QueueRecordService queueRecordService,
			   TranscriptionPlatformService tps, EuropeanaSearchService ess, EuropeanaAnnotationsService eas, ContextMediator contextMediator) {
		// Fired only for crash recovery
		super(record, queueRecordService, tps, ess, eas);
		this.contextMediator = contextMediator;
		this.context = (UpdateTaskContext) contextMediator.get(record);
		this.queueRecordService = queueRecordService;
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
		// we are not reading saved transcriptions from context, as context possibly could not be saved before
		// program crash, thus not containing changes
		transcriptions = record.getTranscriptions();
		state = TaskState.U_GET_TRANSCRIPTION_FROM_TP;
		ContextUtils.executeIfPresent(this.context.getTaskState(),
				() -> this.state = this.context.getTaskState());
	}

	public UpdateTask(String recordIdentifier, String annotationId, String transcriptionId,
					  QueueRecordService queueRecordService, TranscriptionPlatformService tps, EuropeanaSearchService ess, EuropeanaAnnotationsService eas, ContextMediator contextMediator) throws NotFoundException {
		// fired for normal execution
		super(queueRecordService.getRecord(recordIdentifier), queueRecordService, tps, ess, eas);
		this.queueRecordService = queueRecordService;
		Record record = this.queueRecordService.getRecord(recordIdentifier);
		this.contextMediator = contextMediator;
		this.context = (UpdateTaskContext) this.contextMediator.get(record, UpdateTaskContext.class);
		Transcription newTranscription = new Transcription(transcriptionId, record, annotationId);
		record.getTranscriptions().add(newTranscription);
		queueRecordService.saveRecord(record);
		transcriptions = Arrays.asList(newTranscription);
		// state should be changed here and only here
		// moving it earlier could possibly leave us, in case of crash, with task that have no new records, then
		// there is no point for further processing
		queueRecordService.setNewStateForRecord(getRecord().getId(), Record.RecordState.U_PENDING);
		state = TaskState.U_GET_TRANSCRIPTION_FROM_TP;
		ContextUtils.executeIfPresent(this.context.getTaskState(),
				() -> this.state = this.context.getTaskState());
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
				this.context.setTaskState(this.state);
				this.contextMediator.save(this.context);
			case U_HANDLE_TRANSCRIPTION:
				for (Transcription t : transcriptions) {
					// TODO: not working as editing iterated collection on-go
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
				this.context.setTaskState(this.state);
				this.contextMediator.save(this.context);
		}
	}
}
