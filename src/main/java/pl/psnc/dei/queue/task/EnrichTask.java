package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.model.conversion.EnrichTaskContext;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.TranscriptionConverter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enrich task is responsible for adding new transcriptions to existing records
 * Adding transcription to records having transcription will drop new data
 */
public class EnrichTask extends Task {

	private static final Logger logger = LoggerFactory.getLogger(EnrichTask.class);

	private final EnrichTaskContext context;

	private final ContextMediator contextMediator;

	private final Queue<Transcription> notAnnotatedTranscriptions = new LinkedList<>();

	EnrichTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps, EuropeanaSearchService ess, EuropeanaAnnotationsService eas, ContextMediator contextMediator) {
		super(record, queueRecordService, tps, ess, eas);
		this.contextMediator = contextMediator;
		this.context = (EnrichTaskContext) this.contextMediator.get(record);
		state = TaskState.E_GET_TRANSCRIPTIONS_FROM_TP;
		ContextUtils.executeIfPresent(this.context.getTaskState(),
				() -> this.state = this.context.getTaskState());

	}

	@Override
	public void process() {
		switch (state) {
			case E_GET_TRANSCRIPTIONS_FROM_TP:
				logger.info("Task state: E_GET_TRANSCRIPTIONS_FROM_TP");
				getTranscriptionsFromTp();
			case E_HANDLE_TRANSCRIPTIONS:
				logger.info("Task state: E_HANDLE_TRANSCRIPTIONS");
				handleTranscriptions();
			case E_SEND_ANNOTATION_IDS_TO_TP:
				logger.info("Task state: E_SEND_ANNOTATION_IDS_TO_TP");
				sendAnnotationIdsAndFinalizeTask();
		}
		this.contextMediator.delete(this.context, EnrichTaskContext.class);
	}

	/**
	 * Fetch transcription from transcription platform and save them to record and transcription entity
	 */
	private void getTranscriptionsFromTp() {
		Map<String, Transcription> transcriptions = new HashMap<>();
		// fetch transcription from TP
		if(this.context.isHasDownloadedEnrichment()) {
					this.context.getSavedTranscriptions().forEach(
							el -> {
								transcriptions.put(el.getAnnotationId(), el);
							}
					);
		}
		ContextUtils.executeIf(!this.context.isHasDownloadedEnrichment(),
				() -> {
					for (JsonValue val : tps.fetchTranscriptionsFor(record)) {
						try {
							Transcription transcription = new Transcription();
							transcription.setRecord(record);
							transcription.setTpId(val.getAsObject().get("AnnotationId").toString());
							transcription.setTranscriptionContent(TranscriptionConverter.convert(val.getAsObject()));
							JsonValue europeanaAnnotationId = val.getAsObject().get("EuropeanaAnnotationId");
							if (europeanaAnnotationId != null && !"0".equals(europeanaAnnotationId.toString())) {
								transcription.setAnnotationId(europeanaAnnotationId.toString());
							}
							if (queueRecordService.saveTranscriptionIfNotExist(transcription)) {
								transcriptions.put(transcription.getTpId(), transcription);
							}
							transcriptions.put(transcription.getTp_id(), transcription);
						} catch (IllegalArgumentException e) {
							logger.error("Transcription was corrupted: " + val.toString());
						}
					}
					this.queueRecordService.saveTranscriptions(new ArrayList<>(transcriptions.values()));
					this.context.setHasDownloadedEnrichment(true);
					this.context.setSavedTranscriptions(new ArrayList<>(transcriptions.values()));
					this.contextMediator.save(this.context);
		});
		this.removeMissingTranscriptions(new ArrayList<>(transcriptions.values()));
		// add transcription to record
		if (record.getTranscriptions().isEmpty()) {
			logger.info("Transcriptions for record are empty. Adding and saving record.");
			record.getTranscriptions().addAll(transcriptions.values());
			queueRecordService.saveRecord(record);
			// transcriptions that has no transcription id
			fillQueue();
		} else {
			// if record has transcription add context to it
			logger.info("Record already has transcriptions. Processing not annotated.");
			fillQueue();
			for (Transcription transcription : notAnnotatedTranscriptions) {
				Transcription prepared = transcriptions.get(transcription.getTpId());
				if (prepared != null)
					transcription.setTranscriptionContent(prepared.getTranscriptionContent());
			}
		}
		state = TaskState.E_HANDLE_TRANSCRIPTIONS;
		this.context.setTaskState(this.state);
		this.contextMediator.save(this.context);
	}

	/**
	 * Filter records to check if some of them miss their annotations
	 */
	private void fillQueue() {
		notAnnotatedTranscriptions
				.addAll(record.getTranscriptions().stream()
						.filter(e -> StringUtils.isBlank(e.getAnnotationId()))
						.collect(Collectors.toList()));
	}

	/**
	 * Removes all transcriptions present in DB but not fetched from TP
	 *
	 * @param fetchedTranscriptions transcriptions fetched from TP
	 */
	private void removeMissingTranscriptions(List<Transcription> fetchedTranscriptions) {
		List<Transcription> diff = this.record.getTranscriptions().stream()
				.filter(el -> !fetchedTranscriptions.contains(el))
				.collect(Collectors.toList());
		this.record.getTranscriptions().removeAll(diff);
		this.queueRecordService.saveRecord(this.record);
	}

	/**
	 * Fetches annotations id to transcriptions missing it
	 */
	private void handleTranscriptions() {
		// do not saving processed transcriptions in ctx as processed ones cannot become not annotated
		while (!notAnnotatedTranscriptions.isEmpty()) {
			Transcription transcription = notAnnotatedTranscriptions.peek();
			String annotationId = eas.postTranscription(transcription);
			transcription.setAnnotationId(annotationId);
			queueRecordService.saveTranscription(transcription);
			notAnnotatedTranscriptions.remove(transcription);
		}
		state = TaskState.E_SEND_ANNOTATION_IDS_TO_TP;
		this.context.setTaskState(this.state);
		this.contextMediator.save(this.context);
	}

	private void sendAnnotationIdsAndFinalizeTask() {
		Iterator<Transcription> it = record.getTranscriptions().iterator();
		// sending annotations is too quick, thus putting it into db makes allover process slower
		while (it.hasNext()) {
			Transcription t = it.next();
			tps.sendAnnotationUrl(t);
			it.remove();
		}
		record.setState(Record.RecordState.NORMAL);
		queueRecordService.saveRecord(record);
	}

}
