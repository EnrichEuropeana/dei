package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ConversionException;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ConversionContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionTaskContext;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.io.IOException;

public class ConversionTask extends Task {

	private ContextMediator contextMediator;

	private PersistableExceptionService persistableExceptionService;

	Logger logger = LoggerFactory.getLogger(ConversionTask.class);

	private Converter converter;

	private TasksQueueService tqs;

	// Record in JSON-LD
	private JsonObject recordJson;

	// Record in JSON
	private JsonObject recordJsonRaw;

	private TasksFactory tasksFactory;

	private DDBFormatResolver ddbFormatResolver;

	private ConversionTaskContext context;

	ConversionTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaSearchService ess, EuropeanaAnnotationsService eas, DDBFormatResolver ddbfr, TasksQueueService tqs, Converter converter, TasksFactory tasksFactory, PersistableExceptionService persistableExceptionService, ContextMediator contextMediator) {
		super(record, queueRecordService, tps, ess, eas);
		this.persistableExceptionService = persistableExceptionService;
		this.contextMediator = contextMediator;
		this.context = (ConversionTaskContext) this.contextMediator.get(record);
		this.tqs = tqs;
		this.ddbFormatResolver = ddbfr;
		this.converter = converter;
		this.tasksFactory = tasksFactory;

		Aggregator aggregator = record.getAggregator();
		switch (aggregator) {
			case EUROPEANA:
				ContextUtils.executeIfPresent(this.context.getRecordJson(),
						() -> this.recordJson = JSON.parse(this.context.getRecordJson())
				);
				ContextUtils.executeIfNotPresent(this.context.getRecordJson(),
						() -> {
							this.recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
							this.context.setRecordJson(this.recordJson.toString());
							this.contextMediator.save(this.context);
						});
				ContextUtils.executeIfPresent(this.context.getRecordJson(),
						() -> this.recordJsonRaw = JSON.parse(this.context.getRecordJsonRaw())
				);
				ContextUtils.executeIfNotPresent(this.context.getRecordJson(),
						() -> {
							this.recordJsonRaw = ess.retrieveRecordInJson(record.getIdentifier());
							this.context.setRecordJsonRaw(this.recordJsonRaw.toString());
							this.contextMediator.save(this.context);
						});
				break;
			case DDB:
				ContextUtils.executeIfPresent(this.context.getRecordJson(),
						() -> this.recordJson = JSON.parse(this.context.getRecordJson())
				);
				ContextUtils.executeIfNotPresent(this.context.getRecordJson(),
						() -> {
							this.recordJson = ddbfr.getRecordBinariesObject(record.getIdentifier());
							this.context.setRecordJson(this.recordJson.toString());
							this.contextMediator.save(this.context);
						});
				break;
			default:
				throw new IllegalStateException("Unsupported aggregator for conversion.");
		}
	}

	@Override
	public void process() throws Exception {
		try {

			converter.convertAndGenerateManifest(record, recordJson, recordJsonRaw);
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
