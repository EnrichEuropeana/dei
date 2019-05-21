package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ConversionException;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.TasksQueueService;

public class ConversionTask extends Task {

	@Autowired
	private TasksQueueService tqs;

	@Autowired
	private EuropeanaRestService ers;

	private JsonObject recordJson;

	public ConversionTask(Record record) {
		super(record);
		recordJson = ers.retrieveRecordFromEuropeanaAndConvertToJsonLd(record.getIdentifier());
	}

	public ConversionTask(Record record, JsonObject recordJson) {
		super(record);
		this.recordJson = recordJson;
	}

	@Override
	public void process() {
		new Thread(() -> {
			Converter converter = new Converter(record, recordJson);
			try {
				converter.convertAndGenerateManifest();
				tqs.addTaskToQueue(new TranscribeTask(record));
			} catch (ConversionImpossibleException e) {
				try {
					queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
				} catch (NotFoundException ex) {
					throw new AssertionError("Record deleted while being processed, id: " + record.getId()
							+ ", identifier: " + record.getIdentifier(), e);
				}
			} catch (ConversionException e) {
				tqs.addTaskToQueue(new ConversionTask(record, recordJson));
			}
		}).start();
	}
}
