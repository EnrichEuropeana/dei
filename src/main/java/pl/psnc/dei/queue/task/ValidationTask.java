package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.AggregatorException;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ImageNotAvailableException;
import pl.psnc.dei.iiif.InvalidIIIFManifestException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ValidationTaskContext;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.IIIFManifestValidator;
import pl.psnc.dei.util.IiifChecker;

import java.util.Arrays;
import java.util.List;

import static pl.psnc.dei.queue.task.Task.TaskState.*;

public class ValidationTask extends Task {

    private final TasksFactory tasksFactory;

    private final TasksQueueService tqs;

    // Record in JSON-LD
    private JsonObject recordJson;

    private final String serverUrl;

    private final String serverPath;

    private String iiifManifest;

    private final ContextMediator contextMediator;

    private final ValidationTaskContext validationTaskContext;

    private final PersistableExceptionService persistableExceptionService;

    private final ImportProgressService importProgressService;

    private final IIIFManifestValidator iiifManifestValidator;

    private final GeneralRestRequestService generalRestRequestService;

    ValidationTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
            EuropeanaSearchService ess, EuropeanaAnnotationsService eas, TasksQueueService tqs, String url,
            String serverPath, TasksFactory tasksFactory, ContextMediator contextMediator,
            PersistableExceptionService persistableExceptionService, ImportProgressService ips,
            IIIFManifestValidator imv, GeneralRestRequestService grrs) {
        super(record, queueRecordService, tps, ess, eas);
        this.contextMediator = contextMediator;
        this.validationTaskContext = (ValidationTaskContext) this.contextMediator.get(record);
        this.tqs = tqs;
        this.serverUrl = url;
        this.serverPath = serverPath;
        this.state = TaskState.T_RETRIEVE_RECORD;
        ContextUtils.executeIfPresent(this.validationTaskContext.getTaskState(),
                () -> this.state = this.validationTaskContext.getTaskState()
        );
        this.tasksFactory = tasksFactory;
        this.importProgressService = ips;
        this.persistableExceptionService = persistableExceptionService;
        this.iiifManifestValidator = imv;
        this.generalRestRequestService = grrs;
    }

    @Override
    public void process() throws Exception {
        switch (state) {
            case T_RETRIEVE_RECORD:
                // fetch data from europeana
                ContextUtils.executeIfPresent(this.validationTaskContext.getRecordJson(),
                        () -> this.recordJson = JSON.parse(this.validationTaskContext.getRecordJson())
                );
                ContextUtils.executeIfNotPresent(this.validationTaskContext.getRecordJson(),
                        () -> {
                            try {
                                recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
                                this.validationTaskContext.setRecordJson(this.recordJson.toString());
                                this.contextMediator.save(this.validationTaskContext);
                            } catch (AggregatorException aggregatorException) {
                                try {
                                    this.persistException(aggregatorException);
                                    queueRecordService.setNewStateForRecord(record.getId(),
                                            Record.RecordState.V_FAILED);
                                    tps.updateImportState(record.getAnImport());
                                } catch (NotFoundException nfe) {
                                    throw new AssertionError(
                                            "Record deleted while being processed, id: " + record.getId()
                                                    + ", identifier: " + record.getIdentifier(), nfe);
                                }
                                throw new RuntimeException(aggregatorException.getCause());
                            }
                        });
                this.state = V_RETRIEVE_IIIF_MANIFEST;
                this.validationTaskContext.setTaskState(this.state);
                this.contextMediator.save(this.validationTaskContext);
            case V_RETRIEVE_IIIF_MANIFEST:
                if (this.validationTaskContext.isHasThrownError()) {
                    this.persistableExceptionService.findFirstOfAndThrow(
                            List.of(NotFoundException.class),
                            this.validationTaskContext);
                }
                ContextUtils.executeIfNotPresent(this.recordJson,
                        () -> this.recordJson = JSON.parse(this.validationTaskContext.getRecordJson())
                );
                ContextUtils.executeIfPresent(this.validationTaskContext.getIIIFManifest(),
                        () -> this.iiifManifest = this.validationTaskContext.getIIIFManifest()
                );
                ContextUtils.executeIfNotPresent(this.validationTaskContext.getIIIFManifest(),
                        () -> {
                            try {
                                if (StringUtils.isNotBlank(record.getIiifManifest())) {
                                    recordJson.put("iiif_url",
                                            serverUrl + serverPath + "/api/transcription/iiif/manifest?recordId=" +
                                                    record.getIdentifier());
                                    iiifManifest = record.getIiifManifest();
                                } else {
                                    iiifManifest = generalRestRequestService.downloadFrom(
                                            IiifChecker.extractIIIFManifestURL(recordJson, record.getAggregator()));
                                }
                                this.validationTaskContext.setIIIFManifest(this.iiifManifest);
                                this.validationTaskContext.setRecordJson(recordJson.toString());
                                this.contextMediator.save(this.validationTaskContext);
                            } catch (InvalidIIIFManifestException e) {
                                try {
                                    this.persistException(e);
                                    queueRecordService.setNewStateForRecord(record.getId(),
                                            Record.RecordState.V_FAILED);
                                    tps.updateImportState(record.getAnImport());
                                } catch (NotFoundException nfe) {
                                    throw new AssertionError(
                                            "Record deleted while being processed, id: " + record.getId()
                                                    + ", identifier: " + record.getIdentifier(), nfe);
                                }
                                throw new RuntimeException(e.getCause());
                            }
                        });
                this.state = V_VALIDATE_IIIF_MANIFEST;
                this.validationTaskContext.setTaskState(this.state);
                this.contextMediator.save(this.validationTaskContext);
            case V_VALIDATE_IIIF_MANIFEST:
                try {
                    if (this.validationTaskContext.isHasThrownError()) {
                        this.persistableExceptionService.findFirstOfAndThrow(
                                List.of(NotFoundException.class),
                                this.validationTaskContext);
                    }
                    ContextUtils.executeIfNotPresent(this.recordJson,
                            () -> this.recordJson = JSON.parse(this.validationTaskContext.getRecordJson())
                    );
                    ContextUtils.executeIfNotPresent(this.iiifManifest,
                            () -> this.iiifManifest = this.validationTaskContext.getIIIFManifest()
                    );
                    // send validation request
                    ContextUtils.executeIf(!this.validationTaskContext.isHasValidatedManifest(),
                            () -> {
                                iiifManifestValidator.validateIIIFManifest(
                                        IiifChecker.extractIIIFManifestURL(recordJson, record.getAggregator()),
                                        IiifChecker.extractVersion(iiifManifest));
                                this.validationTaskContext.setHasValidatedManifest(true);
                                this.contextMediator.save(this.validationTaskContext);
                            }
                    );
                    this.state = V_CHECK_IMAGES;
                    this.validationTaskContext.setTaskState(this.state);
                    this.contextMediator.save(this.validationTaskContext);
                } catch (NotFoundException | InvalidIIIFManifestException e) {
                    this.persistException(e);
                    queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.V_FAILED);
                    tps.updateImportState(record.getAnImport());
                    throw new RuntimeException(e.getCause());
                }
            case V_CHECK_IMAGES:
                if (this.validationTaskContext.isHasThrownError()) {
                    this.persistableExceptionService.findFirstOfAndThrow(
                            Arrays.asList(NotFoundException.class, InvalidIIIFManifestException.class),
                            this.validationTaskContext);
                }
                ContextUtils.executeIfNotPresent(this.iiifManifest,
                        () -> this.iiifManifest = this.validationTaskContext.getIIIFManifest()
                );
                IiifChecker.extractImages(iiifManifest).stream().map(generalRestRequestService::downloadFrom)
                        .filter(
                                StringUtils::isBlank).findFirst().ifPresentOrElse(s -> {
                            try {
                                this.persistException(new ImageNotAvailableException(
                                        "Images for record " + record.getId() + " not available."));
                                queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.V_FAILED);
                                tps.updateImportState(record.getAnImport());
                            } catch (NotFoundException nfe) {
                                this.contextMediator.delete(this.validationTaskContext);
                                throw new AssertionError("Record deleted while being processed, id: " + record.getId()
                                        + ", identifier: " + record.getIdentifier(), nfe);
                            }
                        }, () -> {
                            importProgressService.reportProgress(record);
                            record.setState(Record.RecordState.T_PENDING);
                            record.setValidated(true);
                            queueRecordService.saveRecord(record);
                            tasksFactory.getTask(record).forEach(tqs::addTaskToQueue);
                            this.contextMediator.delete(this.validationTaskContext);
                        });
        }
    }

    private void persistException(Exception exception) {
        ContextUtils.executeIf(!this.validationTaskContext.isHasThrownError(),
                () -> {
                    this.validationTaskContext.setHasThrownError(true);
                    this.persistableExceptionService.bind(exception, this.validationTaskContext);
                    this.contextMediator.save(this.validationTaskContext);
                });
        ContextUtils.executeIf(!this.validationTaskContext.isHasAddedFailure(),
                () -> {
                    try {
                        tps.addFailure(record.getAnImport().getName(), record, exception);
                        this.validationTaskContext.setHasAddedFailure(true);
                        this.contextMediator.save(this.validationTaskContext);
                    } catch (NotFoundException e1) {
                        throw new AssertionError("Record deleted while being processed, id: " + record.getId()
                                + ", identifier: " + record.getIdentifier(), e1);
                    }
                });
    }
}
