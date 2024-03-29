package pl.psnc.dei.model.conversion;

import pl.psnc.dei.model.Record;
import pl.psnc.dei.queue.task.Task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.ArrayList;

/**
 * Context of Transcribe Task
 */
@Entity
public class TranscribeTaskContext extends Context {
    // PROCESSING STATE STORAGE
    private boolean hasSendRecord;
    private boolean hasThrownError;
    private boolean hasAddedFailure;
    private boolean hasSendCallToAction;

    // PROCESSING DATA STORAGE
    @Column(columnDefinition = "MEDIUMTEXT")
    private String recordJsonRaw;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String recordJson;
    @Enumerated(EnumType.STRING)
    private Task.TaskState taskState;

    public static TranscribeTaskContext from(Record record) {
        TranscribeTaskContext context = new TranscribeTaskContext();
        context.setRecord(record);
        context.setHasSendRecord(false);
        context.setHasThrownError(false);
        context.setHasAddedFailure(false);
        context.setHasSendCallToAction(false);
        context.setRecordJson(null);
        context.setRecordJsonRaw(null);
        context.setTaskState(null);
        context.setExceptions(new ArrayList<>());
        return context;
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

    public boolean isHasSendCallToAction() {
        return hasSendCallToAction;
    }

    public void setHasSendCallToAction(boolean hasSendCallToAction) {
        this.hasSendCallToAction = hasSendCallToAction;
    }

    public String getRecordJson() {
        return recordJson;
    }

    public void setRecordJson(String recordJson) {
        this.recordJson = recordJson;
    }

    public String getRecordJsonRaw() {
        return recordJsonRaw;
    }

    public void setRecordJsonRaw(String recordJsonRaw) {
        this.recordJsonRaw = recordJsonRaw;
    }

    @Override
    public Task.TaskState getTaskState() {
        return taskState;
    }

    @Override
    public void setTaskState(Task.TaskState taskState) {
        this.taskState = taskState;
    }
}
