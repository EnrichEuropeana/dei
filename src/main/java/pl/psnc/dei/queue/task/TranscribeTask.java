package pl.psnc.dei.queue.task;

import pl.psnc.dei.model.Record;

public class TranscribeTask extends Task {

//	For now Strings, JSONs later?
	private String record;
	private String conversionResult;

	public TranscribeTask(Record record) {
		super(record);
		this.state = TaskState.T_RETRIEVE_RECORD;
	}

	@Override
	public void process() throws Exception {
		switch (state) {
			case T_RETRIEVE_RECORD:
//				TODO get data from EU, save it in record | JIRA: EN-59
			case T_CONVERT_RECORD:
//				TODO convert record and save it in conversionResult | JIRA: EN-60
			case T_SEND_RESULT:
//				TODO send conversion result to TP | JIRA: EN-61
		}
	}

}
