package pl.psnc.dei.queue.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.search.EuropeanaSearchService;

/**
 * Factory used to convert records into tasks based on state they are in
 */
@Service
public class TasksFactory {

	@Autowired
	private QueueRecordService qrs;

	@Qualifier("transcriptionPlatformService")
	@Autowired
	private TranscriptionPlatformService tps;

	@Autowired
	private EuropeanaSearchService ess;

	@Autowired
	private EuropeanaAnnotationsService eas;

	@Autowired
	private ContextMediator ctxm;

	@Autowired
	private DDBFormatResolver ddbfr;

	@Autowired
	@Lazy
	private TasksQueueService tqs;

	@Autowired
	private Converter converter;

	@Autowired
	private ImportProgressService ips;

	@Autowired
	private PersistableExceptionService pes;

	@Autowired
	private RecordsRepository recordsRepository;

	@Value("${application.server.url}")
	String serverUrl;

	@Value("${server.servlet.context-path}")
	private String serverPath;

	/**
	 * Converts record basing on it state to proper task
	 * @param record record to convert
	 * @return Task
	 */
	public Task getTask(Record record) {
		switch (record.getState()) {
			case E_PENDING:
				return new EnrichTask(record, qrs, tps, ess, eas, ctxm);
			case T_PENDING:
				return new TranscribeTask(record, qrs, tps, eas, tqs, serverUrl, serverPath, this, ctxm, pes);
			case U_PENDING:
				return new UpdateTask(record, qrs, tps, ess, eas, ctxm);
			case C_PENDING:
				return new ConversionTask(record, qrs, tps, ess, eas, ddbfr, tqs, converter, this, pes, ctxm, recordsRepository);

			default:
				throw new RuntimeException("Incorrect record state!");
		}
	}

	public UpdateTask getNewUpdateTask(String recordId, String annotationId, String transcriptionId) throws NotFoundException {
		return new UpdateTask(recordId, annotationId, transcriptionId, qrs, tps, ess, eas, ctxm);
	}

	/**
	 * Sets task queue service used later on during record -> task conversion
	 * @param tasksQueueService task queue service tos set
	 */
	public void setTasksQueueService(TasksQueueService tasksQueueService) {
		this.tqs = tasksQueueService;
	}

}
