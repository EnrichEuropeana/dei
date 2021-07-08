package pl.psnc.dei.model.conversion;

import org.hibernate.annotations.NaturalId;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UpdateTaskContext extends Context{
    // PROCESSING STATUS STORAGE
    private boolean hasFetchedUpdatedTranscriptions;
    private boolean hasSendUpdates;

    // PROCESSING DATA STORAGE
    @OneToOne
    private Transcription transcription;

    public static UpdateTaskContext from(Record record) {
        UpdateTaskContext context = new UpdateTaskContext();
        context.setRecord(record);
        context.setHasFetchedUpdatedTranscriptions(false);
        context.setHasSendUpdates(false);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
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
