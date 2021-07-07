package pl.psnc.dei.queue.context;

import pl.psnc.dei.queue.task.Task;

public class Context {
    public Task.TaskState getState() {
        return state;
    }

    public void setState(Task.TaskState state) {
        this.state = state;
    }

    private Task.TaskState state;
}
