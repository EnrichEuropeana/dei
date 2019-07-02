package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.queue.TasksQueue;
import pl.psnc.dei.queue.task.Task;
import pl.psnc.dei.queue.task.TasksFactory;

@Service
public class TasksQueueService {
  
	private TasksQueue queue;

	@Autowired
	public TasksQueueService(TasksQueue tasksQueue,QueueRecordService queueRecordService, TasksFactory tasksFactory) {
		queue = tasksQueue;
		tasksFactory.setTasksQueueService(this);
		for (Record record : queueRecordService.getRecordsToProcess()) {
			tasksQueue.addToQueue(tasksFactory.getTask(record));
		}
		new Thread(queue, "TasksQueueThread").start();
	}

	public void addTaskToQueue(Task task) {
		queue.addToQueue(task);
	}
}
