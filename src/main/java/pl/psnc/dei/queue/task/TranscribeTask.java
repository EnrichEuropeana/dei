package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.IiifChecker;

import static pl.psnc.dei.queue.task.Task.TaskState.T_SEND_RESULT;

public class TranscribeTask extends Task {

	private final TasksFactory tasksFactory;

	private TasksQueueService tqs;

	private JsonObject recordJson;

	private String serverUrl;

	TranscribeTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaRestService ers, TasksQueueService tqs, String serverUrl, TasksFactory tasksFactory) {
		super(record, queueRecordService, tps, ers);
		this.tqs = tqs;
		this.state = TaskState.T_RETRIEVE_RECORD;
		this.serverUrl = serverUrl;
		this.tasksFactory = tasksFactory;
	}

	@Override
	public void process() {
		switch (state) {
			case T_RETRIEVE_RECORD:
				recordJson = ers.retrieveRecordFromEuropeanaAndConvertToJsonLd(record.getIdentifier());

				if (IiifChecker.checkIfIiif(recordJson, Aggregator.EUROPEANA)) { //todo add ddb
					state = T_SEND_RESULT;
				} else {
					if (StringUtils.isNotBlank(record.getIiifManifest())) {
						recordJson.put("iiif_url", serverUrl + "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
						state = T_SEND_RESULT;
					} else {
						try {
							queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_PENDING);
							tqs.addTaskToQueue(tasksFactory.getTask(record));
							return;
						} catch (NotFoundException e) {
							throw new AssertionError("Record deleted while being processed, id: " + record.getId()
									+ ", identifier: " + record.getIdentifier());
						}
					}
				}
			case T_SEND_RESULT:
				tps.sendRecord(recordJson);
				try {
					queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.NORMAL);
				} catch (NotFoundException e) {
					throw new AssertionError("Record deleted while being processed, id: " + record.getId()
							+ ", identifier: " + record.getIdentifier(), e);
				}
		}
	}

}
