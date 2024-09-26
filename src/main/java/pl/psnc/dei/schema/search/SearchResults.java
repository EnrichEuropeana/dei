package pl.psnc.dei.schema.search;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import pl.psnc.dei.response.search.Facet;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResults {
    private int totalResults;

    private int resultsCollected;

    @Setter(AccessLevel.NONE)
    private List<Facet> facets = new ArrayList<>();

    private Pagination nextPagination;

    private Pagination defaultPagination;

    @Setter(AccessLevel.NONE)
    private List<SearchResult> results = new ArrayList<>();

    public SearchResults() {
        totalResults = 0;
        resultsCollected = 0;
    }

    public void setFacets(List<Facet> facets) {
        this.facets.clear();
        if (facets != null) {
            this.facets.addAll(facets);
        }
    }

    public void setResults(List<SearchResult> results) {
        this.results.clear();
        if (results != null) {
            this.results.addAll(results);
        }
    }

    public void clear() {
        clearPagination();
        setResultsCollected(0);
        results.clear();
        facets.clear();
        setTotalResults(0);
    }

    private void clearPagination() {
        setNextPagination(null);
        setDefaultPagination(null);
    }
}
