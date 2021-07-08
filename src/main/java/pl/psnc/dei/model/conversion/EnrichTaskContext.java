package pl.psnc.dei.model.conversion;

import org.hibernate.annotations.NaturalId;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;

import javax.persistence.*;
import java.util.List;

@Entity
public class EnrichTaskContext extends Context{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @OneToOne
    private Record record;

    // PROCESSING STATE STORAGE
    private boolean hasDownloadedEnrichment;
    private boolean hasPersistedTranscriptions;
    private boolean hasAnnotatedTranscriptions;

    // PROCESSING DATA STORAGE
    @OneToMany
    private List<Transcription> savedTranscriptions;
    private String fetchedTranscriptions;

    public static EnrichTaskContext from(Record record) {
        EnrichTaskContext context = new EnrichTaskContext();
        context.setRecord(record);
        context.setHasDownloadedEnrichment(false);
        context.setHasPersistedTranscriptions(false);
        context.setHasAnnotatedTranscriptions(false);
        context.setSavedTranscriptions(null);
        context.setFetchedTranscriptions(null);
        return context;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public void setRecord(Record record) {
        this.record = record;
    }

    public boolean isHasDownloadedEnrichment() {
        return hasDownloadedEnrichment;
    }

    public void setHasDownloadedEnrichment(boolean hasDownloadedEnrichment) {
        this.hasDownloadedEnrichment = hasDownloadedEnrichment;
    }

    public boolean isHasPersistedTranscriptions() {
        return hasPersistedTranscriptions;
    }

    public void setHasPersistedTranscriptions(boolean hasPersistedTranscriptions) {
        this.hasPersistedTranscriptions = hasPersistedTranscriptions;
    }

    public boolean isHasAnnotatedTranscriptions() {
        return hasAnnotatedTranscriptions;
    }

    public void setHasAnnotatedTranscriptions(boolean hasAnnotatedTranscriptions) {
        this.hasAnnotatedTranscriptions = hasAnnotatedTranscriptions;
    }

    public List<Transcription> getSavedTranscriptions() {
        return savedTranscriptions;
    }

    public void setSavedTranscriptions(List<Transcription> savedTranscriptions) {
        this.savedTranscriptions = savedTranscriptions;
    }

    public String getFetchedTranscriptions() {
        return fetchedTranscriptions;
    }

    public void setFetchedTranscriptions(String fetchedTranscriptions) {
        this.fetchedTranscriptions = fetchedTranscriptions;
    }
}
