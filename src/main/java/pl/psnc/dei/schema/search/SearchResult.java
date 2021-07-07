package pl.psnc.dei.schema.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.dei.util.IiifAvailability;

@Data
@NoArgsConstructor
public class SearchResult {

    private String id;

    private String imageURL;

    private String title;

    private String author;

    private String issued;

    private String provider;

    private String format;

    private String language;

    private String license;

    private IiifAvailability iiifAvailability;

    private boolean isImported;

    /**
     * URL to object on aggregator portal
     */
    private String sourceObjectURL;
}
