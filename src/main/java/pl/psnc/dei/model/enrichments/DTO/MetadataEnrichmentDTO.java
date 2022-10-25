package pl.psnc.dei.model.enrichments.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataEnrichmentDTO {
    private long id;

    private String attribute;

    private String itemURL;
}
