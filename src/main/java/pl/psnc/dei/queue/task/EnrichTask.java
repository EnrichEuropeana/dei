package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EnrichTask extends Task {

//	String? Or JSON? Maybe additional JSON field for metadata?
	private List<String> transcription;

	private Queue<Transcription> notAnnotatedTranscriptions = new LinkedList<>();

	private Queue<Transcription> annotatedTranscriptions = new LinkedList<>();

	public EnrichTask(Record record) {
		super(record);
		this.state = TaskState.E_GET_TRANSCRIPTIONS_FROM_TP;
		for(Transcription t : record.getTranscriptions()) {
			if(StringUtils.isNotBlank(t.getAnnotationId())) {
				annotatedTranscriptions.add(t);
			} else {
				notAnnotatedTranscriptions.add(t);
			}
		}
	}

	@Override
	public void process() throws Exception {
		switch (state) {
			case E_GET_TRANSCRIPTIONS_FROM_TP:
//				TODO get transcription from TP and save it in memory + DB
				state=TaskState.E_HANDLE_TRANSCRIPTIONS;
			case E_HANDLE_TRANSCRIPTIONS:
//				TODO for every single transcription that have no annotation (notAnnotatedTranscriptions)
//				POST transcription to EU, as response you should get annotationId
//				transcription.setAnnotationId(...)
//				save transcription to DB
//				move transcription from notAnnotated... to annotated...
				state=TaskState.E_SEND_ANNOTATION_IDS_TO_TP;
			case E_SEND_ANNOTATION_IDS_TO_TP:
//				TODO send transcriptions annotation ids to TP,
				queueRecordService.setNewStateForRecord(getRecord().getId(), Record.RecordState.NORMAL);
		}
	}

}
