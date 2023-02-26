package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.AggregatorException;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ImageNotAvailableException;
import pl.psnc.dei.iiif.InvalidIIIFManifestException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.IiifChecker;

import java.util.Arrays;

import static pl.psnc.dei.queue.task.Task.TaskState.T_SEND_CALL_TO_ACTION;
import static pl.psnc.dei.queue.task.Task.TaskState.T_SEND_RESULT;

public class TranscribeTask extends Task {

    private final TasksFactory tasksFactory;

    private final TasksQueueService tqs;

    // Record in JSON-LD
    private JsonObject recordJson;

    // Record in JSON
    private JsonObject recordJsonRaw;

    private final String serverUrl;

    private final String serverPath;

    private final ContextMediator contextMediator;

    private TranscribeTaskContext transcribeTaskContext;

    private final PersistableExceptionService persistableExceptionService;

    private final ImportProgressService importProgressService;

    TranscribeTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
            EuropeanaSearchService ess, EuropeanaAnnotationsService eas, TasksQueueService tqs, String url,
            String serverPath, TasksFactory tasksFactory, ContextMediator contextMediator,
            PersistableExceptionService persistableExceptionService, ImportProgressService ips) {
        super(record, queueRecordService, tps, ess, eas);
        this.contextMediator = contextMediator;
        this.transcribeTaskContext = (TranscribeTaskContext) this.contextMediator.get(record);
        this.tqs = tqs;
        this.serverPath = serverPath;
        this.state = TaskState.T_RETRIEVE_RECORD;
        ContextUtils.executeIfPresent(this.transcribeTaskContext.getTaskState(),
                () -> this.state = this.transcribeTaskContext.getTaskState()
        );
        this.serverUrl = url;
        this.tasksFactory = tasksFactory;
        this.importProgressService = ips;
        this.persistableExceptionService = persistableExceptionService;
    }

    @Override
    public void process() throws Exception {
        switch (state) {
            case T_RETRIEVE_RECORD:
                // fetch data from europeana
                ContextUtils.executeIfPresent(this.transcribeTaskContext.getRecordJson(),
                        () -> this.recordJson = JSON.parse(this.transcribeTaskContext.getRecordJson())
                );
                ContextUtils.executeIfNotPresent(this.transcribeTaskContext.getRecordJson(),
                        () -> {
                            try {
                                recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
                                this.transcribeTaskContext.setRecordJson(this.recordJson.toString());
                                this.contextMediator.save(this.transcribeTaskContext);
                            } catch (AggregatorException aggregatorException) {
                                try {
                                    this.persistException(aggregatorException);
                                    queueRecordService.setNewStateForRecord(record.getId(),
                                            Record.RecordState.T_FAILED);
                                    tps.updateImportState(record.getAnImport());
                                } catch (NotFoundException nfe) {
                                    throw new AssertionError(
                                            "Record deleted while being processed, id: " + record.getId()
                                                    + ", identifier: " + record.getIdentifier(), nfe);
                                }
                                throw new RuntimeException(aggregatorException.getCause());
                            }
                        });
                ContextUtils.executeIfPresent(this.transcribeTaskContext.getRecordJsonRaw(),
                        () -> this.recordJsonRaw = JSON.parse(this.transcribeTaskContext.getRecordJsonRaw())
                );
                ContextUtils.executeIfNotPresent(this.transcribeTaskContext.getRecordJsonRaw(),
                        () -> {
                            try {
                                recordJsonRaw = ess.retrieveRecordInJson(record.getIdentifier());
                                this.transcribeTaskContext.setRecordJsonRaw(this.recordJsonRaw.toString());
                                this.contextMediator.save(this.transcribeTaskContext);
                            } catch (AggregatorException aggregatorException) {
                                try {
                                    this.persistException(aggregatorException);
                                    queueRecordService.setNewStateForRecord(record.getId(),
                                            Record.RecordState.T_FAILED);
                                    tps.updateImportState(record.getAnImport());
                                } catch (NotFoundException nfe) {
                                    throw new AssertionError(
                                            "Record deleted while being processed, id: " + record.getId()
                                                    + ", identifier: " + record.getIdentifier(), nfe);
                                }
                                throw new RuntimeException(aggregatorException.getCause());
                            }
                        });
                // check if fetched data already contains address to iiif
                if (IiifChecker.checkIfIiif(recordJson, Aggregator.EUROPEANA)) { //todo add ddb
                    state = T_SEND_RESULT;
                    importProgressService.reportProgress(record);
                    this.transcribeTaskContext.setTaskState(this.state);
                    this.contextMediator.save(this.transcribeTaskContext);
                } else {
                    if (StringUtils.isNotBlank(record.getIiifManifest())) {
                        // record has no IIIF on europeana, so in previous run was marked as in C_PENDING (see below) and
                        // IIIF was generated, now we need to add manifest to it
                        this.transcribeTaskContext = (TranscribeTaskContext) this.contextMediator.get(record);
                        recordJson.put("iiif_url",
                                serverUrl + serverPath + "/api/transcription/iiif/manifest?recordId=" +
                                        record.getIdentifier());
                        queueRecordService.fillRecordJsonData(record, recordJson, recordJsonRaw);
                        state = T_SEND_RESULT;
                        this.transcribeTaskContext.setTaskState(this.state);
                        this.contextMediator.save(this.transcribeTaskContext);
                    } else {
                        // europeana has no IIIF for this record, thus we generate new and host it on our owns
                        try {
                            queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_PENDING);
                            record.setState(Record.RecordState.C_PENDING);
                            importProgressService.reportProgress(record);
                            tasksFactory.getTask(record).forEach(tqs::addTaskToQueue);
                            return;
                        } catch (NotFoundException e) {
                            throw new AssertionError("Record deleted while being processed, id: " + record.getId()
                                    + ", identifier: " + record.getIdentifier());
                        }
                    }
                }
            case T_SEND_RESULT:
                try {
                    if (this.transcribeTaskContext.isHasThrownError()) {
                        this.persistableExceptionService.findFirstOfAndThrow(
                                Arrays.asList(NotFoundException.class, TranscriptionPlatformException.class),
                                this.transcribeTaskContext);
                    }
                    ContextUtils.executeIfNotPresent(this.recordJson,
                            () -> this.recordJson = JSON.parse(this.transcribeTaskContext.getRecordJson())
                    );
                    ContextUtils.executeIfNotPresent(this.recordJsonRaw,
                            () -> this.recordJsonRaw = JSON.parse(this.transcribeTaskContext.getRecordJsonRaw())
                    );
                    ContextUtils.executeIf(!this.transcribeTaskContext.isHasSendRecord(),
                            () -> {
                                tps.sendRecord(recordJson, record);
                                this.transcribeTaskContext.setHasSendRecord(true);
                                this.contextMediator.save(this.transcribeTaskContext);
                            });
                    if (!record.isValidated()) {
                        try {
                            queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.V_PENDING);
                            record.setState(Record.RecordState.V_PENDING);
                            tasksFactory.getTask(record).forEach(tqs::addTaskToQueue);
                            return;
                        } catch (NotFoundException e) {
                            throw new AssertionError("Record deleted while being processed, id: " + record.getId()
                                    + ", identifier: " + record.getIdentifier());
                        }
                    }

                    this.state = T_SEND_CALL_TO_ACTION;
                    importProgressService.reportProgress(record);
                    this.transcribeTaskContext.setTaskState(this.state);
                    this.contextMediator.save(this.transcribeTaskContext);
                } catch (TranscriptionPlatformException e) {
                    this.persistException(e);
                    // deletion must occur before state change or context will never be deleted
                    this.contextMediator.delete(this.transcribeTaskContext);
                    queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_FAILED);
                    tps.updateImportState(record.getAnImport());
                } catch (NotFoundException e) {
                    this.contextMediator.delete(this.transcribeTaskContext);
                    throw new AssertionError("Record deleted while being processed, id: " + record.getId()
                            + ", identifier: " + record.getIdentifier(), e);
                }
            case T_SEND_CALL_TO_ACTION:
                try {
                    if (this.transcribeTaskContext.isHasThrownError()) {
                        this.persistableExceptionService.findFirstOfAndThrow(
                                Arrays.asList(NotFoundException.class, InvalidIIIFManifestException.class,
                                        ImageNotAvailableException.class, TranscriptionPlatformException.class),
                                this.transcribeTaskContext);
                    }
                    // send validation request
                    ContextUtils.executeIf(!this.transcribeTaskContext.isHasSendCallToAction(),
                            () -> {
                                // send annotation
                                this.eas.postCallToAction(record);
                                this.transcribeTaskContext.setHasSendCallToAction(true);
                                this.contextMediator.save(this.transcribeTaskContext);
                            }
                    );
                    queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_SENT);
                    importProgressService.reportProgress(record);
                    // check if all records are done
                    tps.updateImportState(record.getAnImport());
                    this.transcribeTaskContext.setRecord(record);
                    this.contextMediator.delete(this.transcribeTaskContext);
                } catch (TranscriptionPlatformException e) {
                    this.persistException(e);
                    // deletion must occur before state change or context will never be deleted
                    this.contextMediator.delete(this.transcribeTaskContext);
                    queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_FAILED);
                    tps.updateImportState(record.getAnImport());
                } catch (NotFoundException e) {
                    this.contextMediator.delete(this.transcribeTaskContext);
                    throw new AssertionError("Record deleted while being processed, id: " + record.getId()
                            + ", identifier: " + record.getIdentifier(), e);
                }
                break;
        }
    }

    private void persistException(Exception exception) {
        ContextUtils.executeIf(!this.transcribeTaskContext.isHasThrownError(),
                () -> {
                    this.transcribeTaskContext.setHasThrownError(true);
                    this.persistableExceptionService.bind(exception, this.transcribeTaskContext);
                    this.contextMediator.save(this.transcribeTaskContext);
                });
        ContextUtils.executeIf(!this.transcribeTaskContext.isHasAddedFailure(),
                () -> {
                    try {
                        tps.addFailure(record.getAnImport().getName(), record, exception);
                        this.transcribeTaskContext.setHasAddedFailure(true);
                        this.contextMediator.save(this.transcribeTaskContext);
                    } catch (NotFoundException e1) {
                        throw new AssertionError("Record deleted while being processed, id: " + record.getId()
                                + ", identifier: " + record.getIdentifier(), e1);
                    }
                });
    }
}
