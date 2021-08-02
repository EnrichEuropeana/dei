package pl.psnc.dei.controllers.requests;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class UploadDatasetRequest {

    private @NotNull String europeanaDatasetId;
    private @NotNull String projectName;
    private String dataset;
    private int limit;
    private Set<String> excludedRecords;
}
