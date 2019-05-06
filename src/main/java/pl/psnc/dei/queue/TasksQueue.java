package pl.psnc.dei.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.psnc.dei.exception.TaskCreationException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.queue.task.EnrichTask;
import pl.psnc.dei.queue.task.Task;
import pl.psnc.dei.queue.task.TranscribeTask;
import pl.psnc.dei.queue.task.UpdateTask;
import pl.psnc.dei.service.QueueRecordService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Component
public class TasksQueue implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(TasksQueue.class);

	private Queue<Task> tasks = new ConcurrentLinkedQueue<>();

	private long lastSuccessfulTask = System.currentTimeMillis();

	/**
	 * Waiting time in millis
	 */
	private long waitingTime = 0;

	private long failsCount = 0;

	private static long HOUR = 60 * 60 * 1000;

	private QueueRecordService queueRecordService;

	@Autowired
	public TasksQueue(QueueRecordService queueRecordService) {
		this.queueRecordService = queueRecordService;
		for (Record record : queueRecordService.getRecordsToProcess()) {
			try {
				tasks.add(createTask(record));
			} catch (TaskCreationException e) {
				logger.error("Task creation exception: ", e);
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Task task = tasks.poll();
				if (task == null) {
					synchronized (this) {
						this.wait();
					}
				} else {
					try {
						task.process();
						processingSuccessful();
					} catch (Exception e) {
						tasks.add(task);
						processingFailed();
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("TaskQueue interrupted!", e);
			}
		}
	}

	private void processingSuccessful() {
		lastSuccessfulTask = System.currentTimeMillis();
		failsCount = 0;
		waitingTime = 0;
	}

	private void processingFailed() throws InterruptedException {
		failsCount++;
		if (failsCount > 5) {
			logQueueState();
			if (waitingTime == 0) {
				waitingTime = HOUR;
			} else if (waitingTime == HOUR) {
				waitingTime *= 2;
			} else if (waitingTime < 6 * HOUR) {
				waitingTime *= 3;
			}
			Thread.sleep(waitingTime);
		}
	}

	public void addToQueue(Task task) {
		tasks.add(task);
		notifyAll();
	}

	private void logQueueState() {
		String log = "Queue size: " + tasks.size();
		log += "\nProcessing failed " + failsCount + " in a row";
		log += "\nLast successful try: " + LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSuccessfulTask), ZoneId.systemDefault());
		log += "\nQueue records:\n" + tasks.stream().map(Task::getRecord).map(e -> "(" + e.getId() + " " + e.getState() + ")").collect(Collectors.joining(","));
		logger.info(log);
	}

	private Task createTask(Record record) throws TaskCreationException {
		switch (record.getState()) {
			case E_PENDING:
				return new EnrichTask(record);
			case T_PENDING:
				return new TranscribeTask(record);
			case U_PENDING:
				return new UpdateTask(record);

			default:
				throw new RuntimeException("Incorrect record state!");
		}
	}

}
