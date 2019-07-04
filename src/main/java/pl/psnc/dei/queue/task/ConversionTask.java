package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ConversionException;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.search.EuropeanaSearchService;

public class ConversionTask extends Task {

	private Converter converter;

	private TasksQueueService tqs;

	private JsonObject recordJson;

	private TasksFactory tasksFactory;

	private DDBFormatResolver ddbFormatResolver;

	ConversionTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaSearchService ess, DDBFormatResolver ddbfr, TasksQueueService tqs, Converter converter, TasksFactory tasksFactory) {
		super(record, queueRecordService, tps, ess);
		this.tqs = tqs;
		this.ddbFormatResolver = ddbfr;
		this.converter = converter;
		this.tasksFactory = tasksFactory;

		Aggregator aggregator = record.getAggregator();
		switch (aggregator) {
			case EUROPEANA:
				recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
				break;
			case DDB:
				recordJson = ddbfr.getRecordBinariesObject(record.getIdentifier());
				break;
			default:
				throw new IllegalStateException("Unsupported aggregator for conversion.");
		}
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
