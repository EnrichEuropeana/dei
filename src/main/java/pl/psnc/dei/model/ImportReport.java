package pl.psnc.dei.model;

import java.util.Collections;
import java.util.List;

public class ImportReport {

    private ImportStatus importStatus;
    private List<ImportFailure> importFailure;

    public static ImportReport from(ImportStatus importStatus, List<ImportFailure> importFailure) {
        ImportReport importReport = new ImportReport();
        importReport.setImportFailure(importFailure);
        importReport.setImportStatus(importStatus);
        return importReport;
    }

    public ImportStatus getImportStatus() {
        return importStatus;
    }

    public void setImportStatus(ImportStatus importStatus) {
        this.importStatus = importStatus;
    }

    public List<ImportFailure> getImportFailure() {
        return Collections.unmodifiableList(importFailure);
    }

    public void setImportFailure(List<ImportFailure> importFailure) {
        this.importFailure = importFailure;
    }
}
