package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonValue;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.TranscriptionConverter;

import java.util.*;
import java.util.stream.Collectors;

public class EnrichTask extends Task {

	private Queue<Transcription> notAnnotatedTranscriptions = new LinkedList<>();

	EnrichTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps, EuropeanaRestService ers) {
		super(record, queueRecordService, tps, ers);
	}

	@Override
	public void process() {
		switch (state) {
			case E_GET_TRANSCRIPTIONS_FROM_TP:
				getTranscriptionsFromTp();
				state = TaskState.E_HANDLE_TRANSCRIPTIONS;
			case E_HANDLE_TRANSCRIPTIONS:
				handleTranscriptions();
				state = TaskState.E_SEND_ANNOTATION_IDS_TO_TP;
			case E_SEND_ANNOTATION_IDS_TO_TP:
				sendAnnotationIdsAndFinalizeTask();
		}
	}

	private void getTranscriptionsFromTp() {
		Map<String, Transcription> transcriptions = new HashMap<>();
		for (JsonValue val : tps.fetchTranscriptionsFor(record)) {
			Transcription transcription = new Transcription();
			transcription.setRecord(record);
			transcription.setTp_id(val.getAsObject().get("AnnotationId").toString());
			transcription.setTranscriptionContent(TranscriptionConverter.convert(val.getAsObject()));
			transcriptions.put(transcription.getTp_id(), transcription);
		}
		if (record.getTranscriptions().isEmpty()) {
			record.getTranscriptions().addAll(transcriptions.values());
			queueRecordService.saveRecord(record);
			fillQueue();
		} else {
			fillQueue();
			for (Transcription transcription : notAnnotatedTranscriptions)
				transcription.setTranscriptionContent(transcriptions.get(transcription.getTp_id()).getTranscriptionContent());
		}
	}

	private void fillQueue() {
		notAnnotatedTranscriptions
				.addAll(record.getTranscriptions().stream()
						.filter(e -> StringUtils.isBlank(e.getAnnotationId()))
						.collect(Collectors.toList()));
	}

	private void handleTranscriptions() {
		while (!notAnnotatedTranscriptions.isEmpty()) {
			Transcription transcription = notAnnotatedTranscriptions.peek();
			String annotationId = ers.postTranscription(transcription);
			transcription.setAnnotationId(annotationId);
			queueRecordService.saveTranscription(transcription);
			notAnnotatedTranscriptions.remove(transcription);
		}
	}

	private void sendAnnotationIdsAndFinalizeTask() {
		Iterator<Transcription> it = record.getTranscriptions().iterator();
		while (it.hasNext()) {
			Transcription t = it.next();
			tps.sendAnnotationUrl(t);
			it.remove();
		}
		record.setState(Record.RecordState.NORMAL);
		queueRecordService.saveRecord(record);
	}

}
