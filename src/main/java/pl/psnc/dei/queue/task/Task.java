package pl.psnc.dei.queue.task;

import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.search.EuropeanaSearchService;

@Transactional
public abstract class Task {

	QueueRecordService queueRecordService;
	TranscriptionPlatformService tps;
	EuropeanaSearchService ess;

	protected TaskState state;

	protected Record record;

	Task(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps, EuropeanaSearchService ess) {
		this.record = record;
		this.queueRecordService = queueRecordService;
		this.tps = tps;
		this.ess = ess;
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
