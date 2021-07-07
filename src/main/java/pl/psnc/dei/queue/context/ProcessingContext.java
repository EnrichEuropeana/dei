package pl.psnc.dei.queue.context;

import pl.psnc.dei.queue.task.Task;

public class ProcessingContext extends RecordContext{
    private Task.TaskState processingState;

    public Task.TaskState getProcessingState() {
        return processingState;
    }

    public void setProcessingState(Task.TaskState processingState) {
        this.processingState = processingState;
    }
}
