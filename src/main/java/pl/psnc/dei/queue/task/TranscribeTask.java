package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.TranscriptionPlatformService;

public class TranscribeTask extends Task {

	@Autowired
	private TranscriptionPlatformService tps;

	@Autowired
	private EuropeanaRestService ers;

	private JsonObject recordJson;

	public TranscribeTask(Record record) {
		super(record);
		this.state = TaskState.T_RETRIEVE_RECORD;
	}

	@Override
	public void process() {
		switch (state) {
			case T_RETRIEVE_RECORD:
				recordJson = ers.retriveRecordFromEuropeanaAndConvertToJsonLd(record.getIdentifier());
				state = TaskState.T_SEND_RESULT;
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
