package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.RecordTransferValidationUtil;

import static pl.psnc.dei.queue.task.Task.TaskState.T_SEND_RESULT;

public class TranscribeTask extends Task {

	@Autowired
	private TranscriptionPlatformService tps;

	@Autowired
	private EuropeanaRestService ers;

	@Autowired
	private TasksQueueService tqs;

	private JsonObject recordJson;

	@Value("${application.server.url}")
	private String serverUrl;

	public TranscribeTask(Record record) {
		super(record);
		this.state = TaskState.T_RETRIEVE_RECORD;
	}

	@Override
	public void process() {
		switch (state) {
			case T_RETRIEVE_RECORD:
				recordJson = ers.retrieveRecordFromEuropeanaAndConvertToJsonLd(record.getIdentifier());
				if (RecordTransferValidationUtil.checkIfIiif(recordJson)) {
					state = T_SEND_RESULT;
				} else {
					if (StringUtils.isNotBlank(record.getIiifManifest())) {
						recordJson.put("iiif_url", serverUrl + "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
						state = T_SEND_RESULT;
					} else {
						try {
							queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_PENDING);
							tqs.addTaskToQueue(new ConversionTask(record, recordJson));
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
