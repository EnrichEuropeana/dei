package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
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

	TranscribeTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaSearchService ess, EuropeanaAnnotationsService eas, TasksQueueService tqs, String url, String serverPath, TasksFactory tasksFactory) {
		super(record, queueRecordService, tps, ess, eas);
		this.tqs = tqs;
		this.serverPath = serverPath;
		this.state = TaskState.T_RETRIEVE_RECORD;
		this.serverUrl = url;
		this.tasksFactory = tasksFactory;
	}

	@Override
	public void process() {
		switch (state) {
			case T_RETRIEVE_RECORD:
				// fetch data from europeana
				recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
				recordJsonRaw = ess.retrieveRecordInJson(record.getIdentifier());
				// check if fetched data already contains address to iiif
				if (IiifChecker.checkIfIiif(recordJson, Aggregator.EUROPEANA)) { //todo add ddb
					state = T_SEND_RESULT;
				} else {
					if (StringUtils.isNotBlank(record.getIiifManifest())) {
						recordJson.put("iiif_url", serverUrl + serverPath + "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
						queueRecordService.fillRecordJsonData(record, recordJson, recordJsonRaw);
						state = T_SEND_RESULT;
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
					tps.sendRecord(recordJson, record);
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
