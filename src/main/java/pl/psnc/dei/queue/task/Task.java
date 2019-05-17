package pl.psnc.dei.queue.task;

import org.springframework.beans.factory.annotation.Autowired;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.QueueRecordService;

public abstract class Task {

	@Autowired
	protected static QueueRecordService queueRecordService;

	protected TaskState state;

	protected Record record;

	public Task(Record record) {
		this.record = record;
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
