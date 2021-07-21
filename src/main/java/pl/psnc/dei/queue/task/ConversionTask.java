package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ConversionException;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionTaskContext;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.context.ContextUtils;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.io.IOException;
import java.util.Arrays;

/**
 * Task which try to create an IIIF for given record
 */
public class ConversionTask extends Task {

	private final ContextMediator contextMediator;

	private final PersistableExceptionService persistableExceptionService;

	Logger logger = LoggerFactory.getLogger(ConversionTask.class);

	private final Converter converter;

	private final TasksQueueService tqs;

	// Record in JSON-LD
	private JsonObject recordJson;

	// Record in JSON
	private JsonObject recordJsonRaw;

	private final TasksFactory tasksFactory;

	private ConversionTaskContext context;

	private final RecordsRepository recordsRepository;

	ConversionTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaSearchService ess, EuropeanaAnnotationsService eas, DDBFormatResolver ddbfr, TasksQueueService tqs, Converter converter, TasksFactory tasksFactory, PersistableExceptionService persistableExceptionService, ContextMediator contextMediator, RecordsRepository recordsRepository) {
		super(record, queueRecordService, tps, ess, eas);
		this.persistableExceptionService = persistableExceptionService;
		this.recordsRepository = recordsRepository;
		this.contextMediator = contextMediator;
		this.context = (ConversionTaskContext) this.contextMediator.get(record);
		this.tqs = tqs;
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
				ContextUtils.executeIfPresent(this.context.getRecordJsonRaw(),
						() -> this.recordJsonRaw = JSON.parse(this.context.getRecordJsonRaw())
				);
				ContextUtils.executeIfNotPresent(this.context.getRecordJsonRaw(),
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
		ContextUtils.executeIf(this.context.isHasConverted(),
				() -> {
					record.setState(Record.RecordState.T_PENDING);
					this.recordsRepository.save(record);
					tqs.addTaskToQueue(tasksFactory.getTask(record));
				});
		ContextUtils.executeIf(!this.context.isHasConverted(),
				() -> {
					try {
						this.persistableExceptionService.findFirstOfAndThrow(Arrays.asList(ConversionImpossibleException.class, NotFoundException.class, ConversionException.class, InterruptedException.class), this.context);
						converter.convertAndGenerateManifest(record, recordJson, recordJsonRaw);
						// refetch context as converter.convertAndGenerateManifest(...) will override it
						this.context = (ConversionTaskContext) this.contextMediator.get(this.record);
						this.context.setHasConverted(true);
						record.setState(Record.RecordState.T_PENDING);
						this.recordsRepository.save(record);
						this.contextMediator.save(this.context);
						tqs.addTaskToQueue(tasksFactory.getTask(record));
						this.contextMediator.delete(this.context);
					} catch (ConversionImpossibleException e) {
						logger.info("Impossible to convert record {} {} ", record.getIdentifier(), e);
						try {
							ContextUtils.executeIf(!this.context.isHasThrownException(),
									() -> {
										this.context.setHasThrownException(true);
										this.persistableExceptionService.bind(e, this.context);
										this.contextMediator.save(context);
									});
							ContextUtils.executeIf(!this.context.isHasAddedFailure(),
									() -> {
										try {
											tps.addFailure(record.getAnImport().getName(), record, e.getMessage());
											this.context.setHasAddedFailure(true);
											this.contextMediator.save(this.context);
										} catch (NotFoundException notFoundException) {
											throw new AssertionError("Record deleted while being processed, id: " + record.getId()
													+ ", identifier: " + record.getIdentifier(), e);
										}
									});
							// delete before state change, otherwise ctx could be never deleted as application will
							// never come here
							this.contextMediator.delete(this.context);
							queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
							tps.updateImportState(record.getAnImport());
						} catch (NotFoundException ex) {
							throw new AssertionError("Record deleted while being processed, id: " + record.getId()
									+ ", identifier: " + record.getIdentifier(), e);
						}
					} catch (ConversionException | InterruptedException | IOException e) {
						logger.info("Error while converting record {} {} ", record.getIdentifier(), e);
						try {
							ContextUtils.executeIf(!this.context.isHasThrownException(),
									() -> {
										this.context.setHasThrownException(true);
										this.persistableExceptionService.bind(e, this.context);
										this.contextMediator.save(context);
									});
							ContextUtils.executeIf(!this.context.isHasAddedFailure(),
									() -> {
										try {

											tps.addFailure(record.getAnImport().getName(), record, e.getMessage());
											this.context.setHasAddedFailure(true);
											this.contextMediator.save(this.context);
										} catch (NotFoundException notFoundException) {
											throw new AssertionError("Record deleted while being processed, id: " + record.getId()
													+ ", identifier: " + record.getIdentifier(), e);
										}
									});
							// same as previous
							this.contextMediator.delete(this.context);
							queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_FAILED);
							tps.updateImportState(record.getAnImport());

						} catch (NotFoundException ex) {
							throw new AssertionError("Record deleted while being processed, id: " + record.getId()
									+ ", identifier: " + record.getIdentifier(), e);
						}
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				});
	}
}
