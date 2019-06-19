package pl.psnc.dei.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.psnc.dei.queue.task.Task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Component
public class TasksQueue implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(TasksQueue.class);

	private BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();

	private long lastSuccessfulTask = System.currentTimeMillis();

	/**
	 * Waiting time in millis
	 */
	private long waitingTime = 0;

	private long failsCount = 0;

	private static long HOUR = 60 * 60 * 1000;

	@Override
	public void run() {
		while (true) {
			try {
				Task task = tasks.take();
				try {
					task.process();
					processingSuccessful();
				} catch (Exception e) {
					tasks.add(task);
					processingFailed(e);
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

	private void processingFailed(Exception e) throws InterruptedException {
		failsCount++;
		logger.error("Task processing failed...", e);
		logQueueState();
		if (failsCount > 5) {
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
	}

	private void logQueueState() {
		String log = "Queue size: " + tasks.size();
		log += "\nProcessing failed " + failsCount + " in a row";
		log += "\nLast successful try: " + LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSuccessfulTask), ZoneId.systemDefault());
		log += "\nQueue records:\n" + tasks.stream().map(Task::getRecord).map(e -> "(" + e.getId() + " " + e.getState() + ")").collect(Collectors.joining(","));
		logger.info(log);
	}

}
