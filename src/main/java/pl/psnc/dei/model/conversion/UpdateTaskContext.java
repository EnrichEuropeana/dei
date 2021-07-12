package pl.psnc.dei.model.conversion;

import org.hibernate.annotations.NaturalId;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.queue.task.Task;

import javax.persistence.*;
import java.util.List;

@Entity
public class UpdateTaskContext extends Context{
    // PROCESSING STATUS STORAGE
    private boolean hasFetchedUpdatedTranscriptions;
    private boolean hasSendUpdates;

    // PROCESSING DATA STORAGE
    @OneToOne
    private Transcription transcription;

    public static UpdateTaskContext from(Record record) {
        UpdateTaskContext context = new UpdateTaskContext();
        context.setTaskState(Task.TaskState.U_GET_TRANSCRIPTION_FROM_TP);
        context.setRecord(record);
        context.setHasFetchedUpdatedTranscriptions(false);
        context.setHasSendUpdates(false);
        return context;
    }

    public boolean isHasFetchedUpdatedTranscriptions() {
        return hasFetchedUpdatedTranscriptions;
    }

    public void setHasFetchedUpdatedTranscriptions(boolean hasFetchedUpdatedTranscriptions) {
        this.hasFetchedUpdatedTranscriptions = hasFetchedUpdatedTranscriptions;
    }

    public boolean isHasSendUpdates() {
        return hasSendUpdates;
    }

    public void setHasSendUpdates(boolean hasSendUpdates) {
        this.hasSendUpdates = hasSendUpdates;
    }

    public Transcription getTranscription() {
        return transcription;
    }

    public void setTranscription(Transcription transcription) {
        this.transcription = transcription;
    }
}
