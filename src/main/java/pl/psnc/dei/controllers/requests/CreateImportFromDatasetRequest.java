package pl.psnc.dei.controllers.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateImportFromDatasetRequest extends UploadDatasetRequest {

    private String importName;
    private int importSize;
}
