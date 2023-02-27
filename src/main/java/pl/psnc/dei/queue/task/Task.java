package pl.psnc.dei.queue.task;

import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.search.EuropeanaSearchService;

public abstract class Task {

	protected TaskState state;
	protected final Record record;
	final QueueRecordService queueRecordService;
	final TranscriptionPlatformService tps;
	final EuropeanaSearchService ess;
	final EuropeanaAnnotationsService eas;

	Task(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps, EuropeanaSearchService ess, EuropeanaAnnotationsService eas) {
		this.record = record;
		this.queueRecordService = queueRecordService;
		this.tps = tps;
		this.ess = ess;
		this.eas = eas;
	}

	public Record getRecord() {
		return record;
	}

	public TaskState getTaskState() {
		return state;
	}

	public abstract void process() throws Exception;

	/**
	 * Task states representing task processing progress for:
	 * E - Enrich
	 * T - Transcribe
	 * U - Update
	 */
	public enum TaskState {
		E_GET_TRANSCRIPTIONS_FROM_TP,
		E_HANDLE_TRANSCRIPTIONS,
		E_SEND_ANNOTATION_IDS_TO_TP,
		E_FINALIZE,

		T_RETRIEVE_RECORD,
		T_SEND_RESULT,
		T_SEND_CALL_TO_ACTION,

		V_RETRIEVE_IIIF_MANIFEST,
		V_VALIDATE_IIIF_MANIFEST,
		V_CHECK_IMAGES,

		U_GET_TRANSCRIPTION_FROM_TP,
		U_HANDLE_TRANSCRIPTION,

		M_GET_ENRICHMENTS_FROM_TP,
		M_HANDLE_ENRICHMENTS,
		M_SEND_NOTIFICATIONS,
		M_FINALIZE
	}

}
