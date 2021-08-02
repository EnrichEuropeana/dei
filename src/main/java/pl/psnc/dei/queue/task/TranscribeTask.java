package pl.psnc.dei.queue.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.exception.AggregatorException;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.ImportProgressService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TasksQueueService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.IiifChecker;

import static pl.psnc.dei.queue.task.Task.TaskState.T_SEND_RESULT;

public class TranscribeTask extends Task {

	private final TasksFactory tasksFactory;

	private TasksQueueService tqs;

	// Record in JSON-LD
	private JsonObject recordJson;

	// Record in JSON
	private JsonObject recordJsonRaw;

	private String serverUrl;

	private String serverPath;

	private ImportProgressService importProgressService;

	TranscribeTask(Record record, QueueRecordService queueRecordService, TranscriptionPlatformService tps,
				   EuropeanaSearchService ess, EuropeanaAnnotationsService eas, TasksQueueService tqs, String url,
				   String serverPath, ImportProgressService ips, TasksFactory tasksFactory) {
		super(record, queueRecordService, tps, ess, eas);
		this.tqs = tqs;
		this.serverPath = serverPath;
		this.state = TaskState.T_RETRIEVE_RECORD;
		this.serverUrl = url;
		this.tasksFactory = tasksFactory;
		this.importProgressService = ips;
	}

	@Override
	public void process() {
		switch (state) {
			case T_RETRIEVE_RECORD:
				// fetch data from europeana
				try {
					recordJson = ess.retrieveRecordAndConvertToJsonLd(record.getIdentifier());
					recordJsonRaw = ess.retrieveRecordInJson(record.getIdentifier());
				} catch (AggregatorException aggregatorException) {
					try {
						queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_FAILED);
						tps.addFailure(record.getAnImport().getName(), record, aggregatorException.getMessage());
						tps.updateImportState(record.getAnImport());
					} catch (NotFoundException nfe) {
						throw new AssertionError("Record deleted while being processed, id: " + record.getId()
								+ ", identifier: " + record.getIdentifier(), nfe);
					}
					throw new RuntimeException(aggregatorException.getCause());
				}
				// check if fetched data already contains address to iiif
				if (IiifChecker.checkIfIiif(recordJson, Aggregator.EUROPEANA)) { //todo add ddb
					state = T_SEND_RESULT;
					importProgressService.reportProgress(record);
				} else {
					if (StringUtils.isNotBlank(record.getIiifManifest())) {
						// record has no IIIF on europeana, so in previous run was marked as in C_PENDING (see below) and
						// IIIF was generated, now we need to add manifest to it
						recordJson.put("iiif_url", serverUrl + serverPath + "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
						queueRecordService.fillRecordJsonData(record, recordJson, recordJsonRaw);
						state = T_SEND_RESULT;
					} else {
						// europeana has no IIIF for this record, thus we generate new and host it on our owns
						try {
							queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.C_PENDING);
							record.setState(Record.RecordState.C_PENDING);
							importProgressService.reportProgress(record);
							tqs.addTaskToQueue(tasksFactory.getTask(record));
							return;
						} catch (NotFoundException e) {
							throw new AssertionError("Record deleted while being processed, id: " + record.getId()
									+ ", identifier: " + record.getIdentifier());
						}
					}
				}
			case T_SEND_RESULT:
				try {
					tps.sendRecord(recordJson, record);
					queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_SENT);
					importProgressService.reportProgress(record);
					// check if all records are done
					tps.updateImportState(record.getAnImport());
				} catch (TranscriptionPlatformException e) {
					try {
						// record cannot be send so mark it as faild and fail entire import send
						queueRecordService.setNewStateForRecord(record.getId(), Record.RecordState.T_FAILED);
						tps.addFailure(record.getAnImport().getName(), record, e.getMessage());
						tps.updateImportState(record.getAnImport());
					} catch (NotFoundException e1) {
						throw new AssertionError("Record deleted while being processed, id: " + record.getId()
								+ ", identifier: " + record.getIdentifier(), e1);
					}
				} catch (NotFoundException e) {
					throw new AssertionError("Record deleted while being processed, id: " + record.getId()
							+ ", identifier: " + record.getIdentifier(), e);
				}
		}
	}

}
