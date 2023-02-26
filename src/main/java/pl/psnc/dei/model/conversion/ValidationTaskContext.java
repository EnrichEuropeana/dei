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
public class ValidationTaskContext extends Context {
    // PROCESSING STATE STORAGE
    private boolean hasThrownError;
    private boolean hasAddedFailure;
    private boolean hasValidatedManifest;
    private boolean hasCheckedImages;

    // PROCESSING DATA STORAGE
    @Column(columnDefinition = "MEDIUMTEXT")
    private String recordJsonRaw;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String recordJson;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String iiifManifest;
    @Enumerated(EnumType.STRING)
    private Task.TaskState taskState;

    public static ValidationTaskContext from(Record record) {
        ValidationTaskContext context = new ValidationTaskContext();
        context.setRecord(record);
        context.setHasThrownError(false);
        context.setHasAddedFailure(false);
        context.setHasValidatedManifest(false);
        context.setHasCheckedImages(false);
        context.setRecordJson(null);
        context.setRecordJsonRaw(null);
        context.setIIIFManifest(null);
        context.setTaskState(null);
        context.setExceptions(new ArrayList<>());
        return context;
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

    public boolean isHasValidatedManifest() {
        return hasValidatedManifest;
    }

    public void setHasValidatedManifest(boolean hasValidatedManifest) {
        this.hasValidatedManifest = hasValidatedManifest;
    }

    public boolean isHasCheckedImages() {
        return hasCheckedImages;
    }

    public void setHasCheckedImages(boolean hasCheckedImages) {
        this.hasCheckedImages = hasCheckedImages;
    }

    public String getIIIFManifest() {
        return iiifManifest;
    }

    public void setIIIFManifest(String iiifManifest) {
        this.iiifManifest = iiifManifest;
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
