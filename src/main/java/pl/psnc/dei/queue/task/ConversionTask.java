package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ConversionException;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.DDBFormatResolver;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.io.IOException;

public class ConversionTask extends Task {

	Logger logger = LoggerFactory.getLogger(ConversionTask.class);

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
	public void process() throws Exception {
		try {
			converter.convertAndGenerateManifest(record, recordJson);
			tqs.addTaskToQueue(tasksFactory.getTask(record));
		} catch (ConversionImpossibleException e) {
			logger.info("Impossible to convert record {} {} ", record.getIdentifier(), e);
			try {
				queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
				tps.addFailure(record.getAnImport().getName(), record, e.getMessage());
				tps.updateImportState(record.getAnImport());

			} catch (NotFoundException ex) {
				throw new AssertionError("Record deleted while being processed, id: " + record.getId()
						+ ", identifier: " + record.getIdentifier(), e);
			}
		} catch (ConversionException | InterruptedException | IOException e) {
			logger.info("Error while converting record {} {} ", record.getIdentifier(), e);
			try {
				queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
				tps.addFailure(record.getAnImport().getName(), record, e.getMessage());
				tps.updateImportState(record.getAnImport());

			} catch (NotFoundException ex) {
				throw new AssertionError("Record deleted while being processed, id: " + record.getId()
						+ ", identifier: " + record.getIdentifier(), e);
			}
		}
	}
}
