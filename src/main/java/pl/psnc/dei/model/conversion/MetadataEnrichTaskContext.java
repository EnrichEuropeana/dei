package pl.psnc.dei.model.conversion;

import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.model.Record;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class MetadataEnrichTaskContext extends Context{
    // PROCESSING STATE STORAGE
    private boolean hasDownloadedEnrichment;

    // PROCESSING DATA STORAGE
    @OneToMany
    private List<MetadataEnrichment> savedEnrichments;

    public static MetadataEnrichTaskContext from(Record record) {
        MetadataEnrichTaskContext context = new MetadataEnrichTaskContext();
        context.setRecord(record);
        context.setHasDownloadedEnrichment(false);
        context.setSavedEnrichments(null);
        return context;
    }

    public boolean isHasDownloadedEnrichment() {
        return hasDownloadedEnrichment;
    }

    public void setHasDownloadedEnrichment(boolean hasDownloadedEnrichment) {
        this.hasDownloadedEnrichment = hasDownloadedEnrichment;
    }

    public List<MetadataEnrichment> getSavedEnrichments() {
        return savedEnrichments;
    }

    public void setSavedEnrichments(List<MetadataEnrichment> savedEnrichments) {
        this.savedEnrichments = savedEnrichments;
    }
}
