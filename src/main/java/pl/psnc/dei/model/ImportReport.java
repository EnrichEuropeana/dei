package pl.psnc.dei.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportReport {

    private ImportStatus importStatus;
    private List<ImportFailure> importFailure;

    public static ImportReport from(ImportStatus importStatus, List<ImportFailure> importFailure) {
        ImportReport importReport = new ImportReport();
        importReport.setImportFailure(importFailure);
        importReport.setImportStatus(importStatus);
        return importReport;
    }
}
