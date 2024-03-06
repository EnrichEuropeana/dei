package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.MetadataEnrichTaskContext;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.service.EnrichmentNotifierService;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.MetadataEnrichmentExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enrich task is responsible for adding enrichments to existing records
 */
public class MetadataEnrichTask extends Task {

    private static final Logger logger = LoggerFactory.getLogger(MetadataEnrichTask.class);

    private final MetadataEnrichTaskContext context;

    private final ContextMediator contextMediator;

    private final MetadataEnrichmentExtractor metadataEnrichmentExtractor;

    private final EnrichmentNotifierService ens;

    MetadataEnrichTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
            EuropeanaSearchService ess, EuropeanaAnnotationsService eas, EnrichmentNotifierService ens,
            ContextMediator contextMediator, MetadataEnrichmentExtractor metadataEnrichmentExtractor) {
        super(record, queueRecordService, tps, ess, eas);
        this.contextMediator = contextMediator;
        this.context = (MetadataEnrichTaskContext) this.contextMediator.get(record);
        this.ens = ens;
        state = TaskState.M_GET_ENRICHMENTS_FROM_TP;
        this.metadataEnrichmentExtractor = metadataEnrichmentExtractor;
        ContextUtils.executeIfPresent(this.context.getTaskState(),
                () -> this.state = this.context.getTaskState());
    }

    @Override
    public void process() {
        switch (state) {
            case M_GET_ENRICHMENTS_FROM_TP:
                logger.info("Task state: M_GET_ENRICHMENTS_FROM_TP");
                getEnrichmentsFromTp();
            case M_HANDLE_ENRICHMENTS:
                logger.info("Task state: M_HANDLE_ENRICHMENTS");
                handleEnrichments();
            case M_SEND_NOTIFICATIONS:
                logger.info("Task state: M_SEND_NOTIFICATIONS");
                sendNotificationAndFinalizeTask();
            case M_FINALIZE:
                if (record.getState() == Record.RecordState.ME_PENDING) {
                    record.setState(Record.RecordState.E_PENDING);
                } else {
                    record.setState(Record.RecordState.NORMAL);
                }
                queueRecordService.saveRecord(record);
                this.contextMediator.delete(this.context, MetadataEnrichTaskContext.class);
        }
    }

    /**
     * Fetch enrichments from transcription platform and save them to record and enrichment entity
     */
    private void getEnrichmentsFromTp() {
        List<MetadataEnrichment> enrichments = new ArrayList<>();

        // fetch enrichments from TP
        if (this.context.isHasDownloadedEnrichment()) {
            enrichments.addAll(this.context.getSavedEnrichments());
        }
        ContextUtils.executeIf(!this.context.isHasDownloadedEnrichment(),
                () -> {
                    JsonObject metadataEnrichments = tps.fetchMetadataEnrichmentsFor(record).getAsArray().get(0)
                            .getAsObject();
                    enrichments.addAll(
                            metadataEnrichmentExtractor.extractEnrichments(record, metadataEnrichments).stream().filter(
                                    queueRecordService::saveMetadataEnrichmentIfNotExist).collect(Collectors.toList()));

                    this.queueRecordService.saveMetadataEnrichments(new ArrayList<>(enrichments));
                    this.context.setHasDownloadedEnrichment(true);
                    this.context.setSavedEnrichments(new ArrayList<>(enrichments));
                    this.contextMediator.save(this.context);
                });
        state = TaskState.M_HANDLE_ENRICHMENTS;
        this.context.setTaskState(this.state);
        this.contextMediator.save(this.context);
    }

    /**
     * Combines all enrichments for the record to form an EDM and stores it in DB
     * Json version is not stored, it is generated ad-hoc
     */
    private void handleEnrichments() {
        // temporarily empty step
        state = TaskState.M_SEND_NOTIFICATIONS;
        this.context.setTaskState(this.state);
        this.contextMediator.save(this.context);
    }

    private void sendNotificationAndFinalizeTask() {
        ens.notifyPublishers(record);
        state = TaskState.E_FINALIZE;
        this.context.setTaskState(this.state);
        this.contextMediator.save(this.context);
    }
}
