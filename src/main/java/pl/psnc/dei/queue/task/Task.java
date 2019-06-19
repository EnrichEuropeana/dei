package pl.psnc.dei.queue.task;

import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;

public abstract class Task {

	QueueRecordService queueRecordService;
	TranscriptionPlatformService tps;
	EuropeanaRestService ers;

	protected TaskState state;

	protected Record record;

	Task(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps, EuropeanaRestService ers) {
		this.record = record;
		this.queueRecordService = queueRecordService;
		this.tps = tps;
		this.ers = ers;
	}

	public Record getRecord() {
		return record;
	}

	public abstract void process();

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

		T_RETRIEVE_RECORD,
		T_SEND_RESULT,

		U_GET_TRANSCRIPTION_FROM_TP,
		U_HANDLE_TRANSCRIPTION
	}

}
