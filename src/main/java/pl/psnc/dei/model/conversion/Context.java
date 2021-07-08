package pl.psnc.dei.model.conversion;

import pl.psnc.dei.model.Record;
import pl.psnc.dei.queue.task.Task;

public abstract class Context {
    private Record.RecordState recordState;
    private Task.TaskState taskState;

    public Record.RecordState getRecordState() {
        return recordState;
    }

    public void setRecordState(Record.RecordState recordState) {
        this.recordState = recordState;
    }

    public Task.TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(Task.TaskState taskState) {
        this.taskState = taskState;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }
}
