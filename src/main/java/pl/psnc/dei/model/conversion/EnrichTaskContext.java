package pl.psnc.dei.model.conversion;

import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class EnrichTaskContext extends Context{
    // PROCESSING STATE STORAGE
    private boolean hasDownloadedEnrichment;

    // PROCESSING DATA STORAGE
    @OneToMany
    private List<Transcription> savedTranscriptions;

    public static EnrichTaskContext from(Record record) {
        EnrichTaskContext context = new EnrichTaskContext();
        context.setRecord(record);
        context.setHasDownloadedEnrichment(false);
        context.setSavedTranscriptions(null);
        return context;
    }

    public boolean isHasDownloadedEnrichment() {
        return hasDownloadedEnrichment;
    }

    public void setHasDownloadedEnrichment(boolean hasDownloadedEnrichment) {
        this.hasDownloadedEnrichment = hasDownloadedEnrichment;
    }

    public List<Transcription> getSavedTranscriptions() {
        return savedTranscriptions;
    }

    public void setSavedTranscriptions(List<Transcription> savedTranscriptions) {
        this.savedTranscriptions = savedTranscriptions;
    }
}
