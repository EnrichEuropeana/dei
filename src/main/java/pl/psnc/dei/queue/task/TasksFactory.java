package pl.psnc.dei.queue.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.search.EuropeanaSearchService;

@Service
public class TasksFactory {

	@Autowired
	private QueueRecordService qrs;

	@Autowired
	private TranscriptionPlatformService tps;

	@Autowired
	private EuropeanaSearchService ess;

	@Autowired
	private EuropeanaAnnotationsService eas;

	@Autowired
	private DDBFormatResolver ddbfr;

	@Autowired
	@Lazy
	private TasksQueueService tqs;

	@Autowired
	private Converter converter;

	@Value("${application.server.url}")
	String serverUrl;

	@Value("${server.servlet.context-path}")
	private String serverPath;

	public Task getTask(Record record) {
		switch (record.getState()) {
			case E_PENDING:
				return new EnrichTask(record, qrs, tps, ess, eas);
			case T_PENDING:
				return new TranscribeTask(record, qrs, tps, ess, eas, tqs, serverUrl, serverPath, this);
			case U_PENDING:
				return new UpdateTask(record, qrs, tps, ess, eas);
			case C_PENDING:
				return new ConversionTask(record, qrs, tps, ess, eas, ddbfr, tqs, converter, this);

			default:
				throw new RuntimeException("Incorrect record state!");
		}
	}

	public UpdateTask getNewUpdateTask(String recordId, String annotationId, String transcriptionId) throws NotFoundException {
		return new UpdateTask(recordId, annotationId, transcriptionId, qrs, tps, ess, eas);
	}

	public void setTasksQueueService(TasksQueueService tasksQueueService) {
		this.tqs = tasksQueueService;
	}

}
