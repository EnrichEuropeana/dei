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
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.io.IOException;

/**
 * Task which try to create an IIIF for given record
 */
public class ConversionTask extends Task {

	Logger logger = LoggerFactory.getLogger(ConversionTask.class);

	private Converter converter;

	private TasksQueueService tqs;

	// Record in JSON-LD
	private JsonObject recordJson;

	// Record in JSON
	private JsonObject recordJsonRaw;

	private TasksFactory tasksFactory;

	private DDBFormatResolver ddbFormatResolver;

	private ImportProgressService importProgressService;

	ConversionTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaSearchService ess, EuropeanaAnnotationsService eas, DDBFormatResolver ddbfr,
				   TasksQueueService tqs, Converter converter, ImportProgressService ips, TasksFactory tasksFactory) {
		super(record, queueRecordService, tps, ess, eas);
		this.tqs = tqs;
		this.ddbFormatResolver = ddbfr;
		this.converter = converter;
		this.tasksFactory = tasksFactory;
		this.importProgressService = ips;

		Aggregator aggregator = record.getAggregator();
		switch (aggregator) {
			case EUROPEANA:
				// fetch record data
				recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
				recordJsonRaw = ess.retrieveRecordInJson(record.getIdentifier());
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
			converter.convertAndGenerateManifest(record, recordJson, recordJsonRaw);
			importProgressService.reportProgress(record);
			// readd task to queue for further processing
			// previously processing ended with state C_PENDIGN due to IIIF creation
			tqs.addTaskToQueue(tasksFactory.getTask(record));
		} catch (ConversionImpossibleException e) {
			logger.info("Impossible to convert record {} {} ", record.getIdentifier(), e);
			try {
				// if IIIF generation failed then failure must be added to record and entire Import Sending ends with failure
				queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
				// update record
				tps.addFailure(record.getAnImport().getName(), record, e);
				// fail import due to change in import state from IN_PROGRESS to FAILED
				tps.updateImportState(record.getAnImport());

			} catch (NotFoundException ex) {
				throw new AssertionError("Record deleted while being processed, id: " + record.getId()
						+ ", identifier: " + record.getIdentifier(), e);
			}
		} catch (ConversionException | InterruptedException | IOException e) {
			// same as previous
			logger.info("Error while converting record {} {} ", record.getIdentifier(), e);
			try {
				queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
				tps.addFailure(record.getAnImport().getName(), record, e);
				tps.updateImportState(record.getAnImport());

			} catch (NotFoundException ex) {
				throw new AssertionError("Record deleted while being processed, id: " + record.getId()
						+ ", identifier: " + record.getIdentifier(), e);
			}
		}
	}
}
