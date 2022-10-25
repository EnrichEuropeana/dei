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
import pl.psnc.dei.util.MetadataEnrichmentExtractor;
import pl.psnc.dei.util.TranscriptionConverter;

import java.util.List;

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
	private Converter cnv;

	@Autowired
	private ImportProgressService ips;

	@Autowired
	private PersistableExceptionService pes;

	@Autowired
	private RecordsRepository rr;

	@Autowired
	private TranscriptionConverter tc;

	@Autowired
	private EnrichmentNotifierService ens;

	@Autowired
	private MetadataEnrichmentExtractor mee;

	@Value("${application.server.url}")
	String serverUrl;

	@Value("${server.servlet.context-path}")
	private String serverPath;

	/**
	 * Converts record basing on it state to proper task
	 * @param record record to convert
	 * @return Task
	 */
	public List<Task> getTask(Record record) {
		switch (record.getState()) {
			case E_PENDING:
				return List.of(new EnrichTask(record, qrs, tps, ess, eas, ens, ctxm, tc));
			case T_PENDING:
				return List.of(new TranscribeTask(record, qrs, tps, ess, eas, tqs, serverUrl, serverPath, this, ctxm, pes, ips));
			case U_PENDING:
				return List.of(new UpdateTask(record, qrs, tps, ess, eas, ens, ctxm));
			case C_PENDING:
				return List.of(new ConversionTask(record, qrs, tps, ess, eas, ddbfr, tqs, cnv, ips, this, pes, rr, ctxm));
			case M_PENDING:
				return List.of(new MetadataEnrichTask(record, qrs, tps, ess, eas, ens, ctxm, mee));
			case ME_PENDING:
				return List.of(new EnrichTask(record, qrs, tps, ess, eas, ens, ctxm, tc),
						new MetadataEnrichTask(record, qrs, tps, ess, eas, ens, ctxm, mee));

			default:
				throw new RuntimeException("Incorrect record state!");
		}
	}

	public UpdateTask getNewUpdateTask(String recordId, String annotationId, String transcriptionId) throws NotFoundException {
		return new UpdateTask(recordId, annotationId, transcriptionId, qrs, tps, ess, eas, ens, ctxm);
	}

	/**
	 * Sets task queue service used later on during record -> task conversion
	 * @param tasksQueueService task queue service tos set
	 */
	public void setTasksQueueService(TasksQueueService tasksQueueService) {
		this.tqs = tasksQueueService;
	}

}
