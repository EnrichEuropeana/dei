package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ConversionException;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;

public class ConversionTask extends Task {

	private Converter converter;

	private TasksQueueService tqs;

	private JsonObject recordJson;

	private TasksFactory tasksFactory;

	ConversionTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaRestService ers, TasksQueueService tqs, Converter converter, TasksFactory tasksFactory) {
		super(record, queueRecordService, tps, ers);
		this.tqs = tqs;
		this.converter = converter;
		this.tasksFactory = tasksFactory;
		recordJson = ers.retrieveRecordFromEuropeanaAndConvertToJsonLd(record.getIdentifier());
	}

	@Override
	public void process() {
		new Thread(() -> {
			try {
				converter.convertAndGenerateManifest(record, recordJson);
				tqs.addTaskToQueue(tasksFactory.getTask(record));
			} catch (ConversionImpossibleException e) {
				try {
					queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
				} catch (NotFoundException ex) {
					throw new AssertionError("Record deleted while being processed, id: " + record.getId()
							+ ", identifier: " + record.getIdentifier(), e);
				}
			} catch (ConversionException e) {
				tqs.addTaskToQueue(this);
			}
		}).start();
	}
}
