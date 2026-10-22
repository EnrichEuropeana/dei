package pl.psnc.dei.model.enrichments.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataEnrichmentDTO {
    protected final static String WIKIDATA_URL = "https://www.wikidata.org/wiki/%s";

    protected final static Pattern WIKIDATA_ID_PATTERN = Pattern.compile("Q[0-9]+$");

    private long id;

    private String attribute;

    private TranscribathonItemDTO item;
}
