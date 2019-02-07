package pl.psnc.dei.schema.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.psnc.dei.response.search.Facet;

import java.util.List;

public class SearchResults {
    @JsonProperty("totalResults")
    private Integer totalResults;

    @JsonProperty("facets")
    private List<Facet> facets = null;

    @JsonProperty("nextCursor")
    private String nextCursor;

    @JsonProperty("results")
    private List<SearchResult> results = null;
}
