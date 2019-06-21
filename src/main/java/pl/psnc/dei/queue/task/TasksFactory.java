package pl.psnc.dei.queue.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.*;

@Service
public class TasksFactory {

	@Autowired
	private QueueRecordService qrs;

	@Autowired
	private TranscriptionPlatformService tps;

	@Autowired
	private EuropeanaRestService ers;

	@Autowired
	private DDBFormatResolver ddbfr;

	private TasksQueueService tqs;

	@Autowired
	private Converter converter;

	@Value("${application.server.url}")
	String serverUrl;

	public Task getTask(Record record) {
		switch (record.getState()) {
			case E_PENDING:
				return new EnrichTask(record, qrs, tps, ers);
			case T_PENDING:
				return new TranscribeTask(record, qrs, tps, ers, tqs, serverUrl, this);
			case U_PENDING:
				return new UpdateTask(record, qrs, tps, ers);
			case C_PENDING:
				return new ConversionTask(record, qrs, tps, ers, ddbfr, tqs, converter, this);

			default:
				throw new RuntimeException("Incorrect record state!");
		}
	}

	public UpdateTask getNewUpdateTask(String recordId, String annotationId, String transcriptionId) throws NotFoundException {
		return new UpdateTask(recordId, annotationId, transcriptionId, qrs, tps, ers);
	}

	public void setTasksQueueService(TasksQueueService tasksQueueService) {
		this.tqs = tasksQueueService;
	}

}
