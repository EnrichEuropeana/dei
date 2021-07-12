package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.IiifChecker;

import static pl.psnc.dei.queue.task.Task.TaskState.T_SEND_RESULT;

public class TranscribeTask extends Task {

	private final TasksFactory tasksFactory;

	private TasksQueueService tqs;

	// Record in JSON-LD
	private JsonObject recordJson;

	// Record in JSON
	private JsonObject recordJsonRaw;

	private String serverUrl;

	private String serverPath;

	private ContextMediator contextMediator;

	private TranscribeTaskContext transcribeTaskContext;

	TranscribeTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaSearchService ess, EuropeanaAnnotationsService eas, TasksQueueService tqs, String url, String serverPath, TasksFactory tasksFactory, ContextMediator contextMediator) {
		super(record, queueRecordService, tps, ess, eas);
		this.contextMediator = contextMediator;
		this.transcribeTaskContext = (TranscribeTaskContext) this.contextMediator.get(record);
		this.tqs = tqs;
		this.serverPath = serverPath;
		this.state = TaskState.T_RETRIEVE_RECORD;
		ContextUtils.setIfPresent(this.state, this.transcribeTaskContext.getTaskState());
		this.serverUrl = url;
		this.tasksFactory = tasksFactory;
	}

	@Override
	public void process() {
		switch (state) {
			case T_RETRIEVE_RECORD:
				ContextUtils.setIfPresent(this.recordJson, this.transcribeTaskContext.getRecordJson());
				ContextUtils.executeIfNotPresent(this.transcribeTaskContext.getRecordJson(),
						() -> {
							recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
							this.transcribeTaskContext.setRecordJson(this.recordJson.toString());
							this.contextMediator.save(this.transcribeTaskContext);
						});
				ContextUtils.setIfPresent(this.recordJson, this.transcribeTaskContext.getRecordJsonRaw());
				ContextUtils.executeIfNotPresent(this.transcribeTaskContext.getRecordJsonRaw(),
						() -> {
							recordJsonRaw = ess.retrieveRecordInJson(record.getIdentifier());
							this.transcribeTaskContext.setRecordJsonRaw(this.recordJsonRaw.toString());
							this.contextMediator.save(this.transcribeTaskContext);
						});
				if (IiifChecker.checkIfIiif(recordJson, Aggregator.EUROPEANA)) { //todo add ddb
					state = T_SEND_RESULT;
					this.transcribeTaskContext.setTaskState(this.state);
					this.contextMediator.save(this.transcribeTaskContext);
				} else {
					if (StringUtils.isNotBlank(record.getIiifManifest())) {
						recordJson.put("iiif_url", serverUrl + serverPath + "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
						queueRecordService.fillRecordJsonData(record, recordJson, recordJsonRaw);
						state = T_SEND_RESULT;
						this.transcribeTaskContext.setTaskState(this.state);
						this.contextMediator.save(this.transcribeTaskContext);
					} else {
						try {
							queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_PENDING);
							record.setState(Record.RecordState.C_PENDING);
							tqs.addTaskToQueue(tasksFactory.getTask(record));
							return;
						} catch (NotFoundException e) {
							throw new AssertionError("Record deleted while being processed, id: " + record.getId()
									+ ", identifier: " + record.getIdentifier());
						}
					}
				}
			case T_SEND_RESULT:
				try {
					ContextUtils.executeIf(!this.transcribeTaskContext.isHasSendRecord(),
							() -> {
								tps.sendRecord(recordJson, record);
								this.transcribeTaskContext.setHasSendRecord(true);
								this.contextMediator.save(this.transcribeTaskContext);
							});
					queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_SENT);
					tps.updateImportState(record.getAnImport());
				} catch (TranscriptionPlatformException e) {
					try {
						queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_FAILED);
						tps.addFailure(record.getAnImport().getName(), record, e.getMessage());
						tps.updateImportState(record.getAnImport());
					} catch (NotFoundException e1) {
						throw new AssertionError("Record deleted while being processed, id: " + record.getId()
								+ ", identifier: " + record.getIdentifier(), e1);
					}
				} catch (NotFoundException e) {
					throw new AssertionError("Record deleted while being processed, id: " + record.getId()
							+ ", identifier: " + record.getIdentifier(), e);
				}
		}
	}

}
