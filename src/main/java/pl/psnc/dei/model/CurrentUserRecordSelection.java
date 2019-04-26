package pl.psnc.dei.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about currently selected project, dataset and records;
 * This is Session scoped bean (will be removed from memory at the session termination)
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUserRecordSelection {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserRecordSelection.class);

    private Project selectedProject;
    private Dataset selectedDataSet;
    private List<String> selectedRecordIds = new ArrayList<>();
    private List<String> selectedRecordIdsForImport = new ArrayList<>();

    public Project getSelectedProject() {
        return selectedProject;
    }

    public Dataset getSelectedDataSet() {
        return selectedDataSet;
    }

    public void setSelectedDataSet(Dataset selectedDataSet) {
        log.info("Changing user selected dataset to: " + selectedDataSet);
        this.selectedDataSet = selectedDataSet;
    }

    public void setSelectedProject(Project selectedProject) {
        log.info("Changing user selected project to: " + selectedProject);
        this.selectedProject = selectedProject;
        setSelectedDataSet(null);
    }

    public boolean isRecordSelected(String recordId) {
        return selectedRecordIds.contains(recordId);
    }

    public boolean isRecordSelectedForImport(String recordId) {
        return selectedRecordIdsForImport.contains(recordId);
    }

    public List<String> getSelectedRecordIds() {
        return selectedRecordIds;
    }

    public List<String> getSelectedRecordIdsForImport() {
        return selectedRecordIdsForImport;
    }

    public void addSelectedRecordId(String recordId) {
        log.info("Adding new record id ({}) to selected records list", recordId);
        selectedRecordIds.add(recordId);
    }

    public void removeSelectedRecordId(String recordId) {
        log.info("Removing record id ({}) from selected records list", recordId);
        selectedRecordIds.remove(recordId);
    }

    public void clearSelectedRecords() {
        log.info("Removing all records");
        selectedRecordIds = new ArrayList<>();
    }

    public void addSelectedRecordIdForImport(String recordId) {
        log.info("Adding new record id ({}) to import", recordId);
        selectedRecordIdsForImport.add(recordId);
    }

    public void removeSelectedRecordIdForImport(String recordId) {
        log.info("Removing record id ({}) from import", recordId);
        selectedRecordIdsForImport.remove(recordId);
    }

    public void clearSelectedRecordsForImport() {
        log.info("Removing all records from import");
        selectedRecordIdsForImport = new ArrayList<>();
    }
}
