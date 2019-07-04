package pl.psnc.dei.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

/**
 * Stores information about currently selected project, dataset and records;
 * This is Session scoped bean (will be removed from memory at the session termination)
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUserRecordSelection {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserRecordSelection.class);

    private Aggregator aggregator;
    private Project selectedProject;
    private Dataset selectedDataSet;
    private Set<Record> selectedRecords = new HashSet<>();
    private List<Record> selectedRecordsForImport = new ArrayList<>();

    public Aggregator getAggregator() {
        return aggregator;
    }

    public void setAggregator(Aggregator aggregator) {
        log.info("Changing user selected aggregator to: {}", aggregator.getFullName());
        this.aggregator = aggregator;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public Dataset getSelectedDataSet() {
        return selectedDataSet;
    }

    public void setSelectedDataSet(Dataset selectedDataSet) {
        log.info("Changing user selected dataset to: {}", selectedDataSet);
        this.selectedDataSet = selectedDataSet;
    }

    public void setSelectedProject(Project selectedProject) {
        log.info("Changing user selected project to: {}", selectedProject);
        this.selectedProject = selectedProject;
        setSelectedDataSet(null);
    }

    public boolean isRecordSelected(String recordId) {
        return selectedRecords.contains(recordId);
    }

    public boolean isRecordSelectedForImport(String recordId) {
        return selectedRecordsForImport.contains(recordId);
    }

    public Set<Record> getSelectedRecords() {
        return selectedRecords;
    }

    public List<Record> getSelectedRecordsForImport() {
        return Collections.unmodifiableList(selectedRecordsForImport);
    }

    public void addSelectedRecord(Record record) {
        log.info("Adding new record id ({}) to selected records set", record);
        selectedRecords.add(record);
    }

    public void removeSelectedRecordId(String recordId) {
        log.info("Removing record id ({}) from selected records set", recordId);
        selectedRecords.removeIf((record -> record.getIdentifier().equals(recordId)));
    }

    public void clearSelectedRecords() {
        log.info("Removing all records");
        selectedRecords = new HashSet<>();
    }

    public void addSelectedRecordIdForImport(Record record) {
        log.info("Adding new record id ({}) to import", record);
        selectedRecordsForImport.add(record);
    }

    public void removeSelectedRecordIdForImport(String recordId) {
        log.info("Removing record id ({}) from import", recordId);
        selectedRecordsForImport.remove(recordId);
    }

    public void clearSelectedRecordsForImport() {
        log.info("Removing all records from import");
        selectedRecordsForImport = new ArrayList<>();
    }
}
