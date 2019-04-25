package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.queue.TasksQueue;
import pl.psnc.dei.queue.task.Task;

@Service
public class TasksQueueService {
	
	private static TasksQueue queue;

	@Autowired
	public TasksQueueService(TasksQueue tasksQueue) {
		queue = tasksQueue;
		new Thread(queue, "TasksQueueThread").start();
	}

	//	This method should be called from JIRA issues: EN-75, EN-70, EN-87
	public void addTaskToQueue(Task task) {
		queue.addToQueue(task);
	}
}
