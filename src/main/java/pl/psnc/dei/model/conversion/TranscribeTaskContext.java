package pl.psnc.dei.model.conversion;

import org.hibernate.annotations.NaturalId;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.queue.task.Task;

import javax.persistence.*;

/**
 * Context of Transcribe Task
 */
@Entity
public class TranscribeTaskContext extends Context{
    // PROCESSING STATE STORAGE
    private boolean hasJson;
    private boolean hasSendRecord;
    private boolean hasThrownError;
    private boolean hasAddedFailure;

    // PROCESSING DATA STORAGE
    private String recordJsonRaw;
    private String recordJson;
    @Enumerated(EnumType.STRING)
    private Task.TaskState taskState;
    private Exception exception;

    public static TranscribeTaskContext from(Record record) {
        TranscribeTaskContext context = new TranscribeTaskContext();
        context.setRecord(record);
        context.setHasJson(false);
        context.setHasSendRecord(false);
        context.setHasThrownError(false);
        context.setHasAddedFailure(false);
        context.setRecordJson("");
        context.setRecordJsonRaw("");
        context.setTaskState(null);
        context.setException(null);
        return context;
    }

    public boolean isHasJson() {
        return hasJson;
    }

    public void setHasJson(boolean hasJson) {
        this.hasJson = hasJson;
    }

    public boolean isHasSendRecord() {
        return hasSendRecord;
    }

    public void setHasSendRecord(boolean hasSendRecord) {
        this.hasSendRecord = hasSendRecord;
    }

    public boolean isHasThrownError() {
        return hasThrownError;
    }

    public void setHasThrownError(boolean hasThrownError) {
        this.hasThrownError = hasThrownError;
    }

    public boolean isHasAddedFailure() {
        return hasAddedFailure;
    }

    public void setHasAddedFailure(boolean hasAddedFailure) {
        this.hasAddedFailure = hasAddedFailure;
    }

    public String getRecordJsonRaw() {
        return recordJsonRaw;
    }

    public void setRecordJsonRaw(String recordJsonRaw) {
        this.recordJsonRaw = recordJsonRaw;
    }

    public String getRecordJson() {
        return recordJson;
    }

    public void setRecordJson(String recordJson) {
        this.recordJson = recordJson;
    }

    @Override
    public Task.TaskState getTaskState() {
        return taskState;
    }

    @Override
    public void setTaskState(Task.TaskState taskState) {
        this.taskState = taskState;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
