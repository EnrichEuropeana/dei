package pl.psnc.dei.schema.search;

import pl.psnc.dei.response.search.Facet;

import java.util.ArrayList;
import java.util.List;

public class SearchResults {
    private int totalResults;

    private int resultsCollected;

    private List<Facet> facets = new ArrayList<>();

    private Pagination nextPagination;

    private Pagination defaultPagination;

    private List<SearchResult> results = new ArrayList<>();

    public SearchResults() {
        totalResults = 0;
        resultsCollected = 0;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getResultsCollected() {
        return resultsCollected;
    }

    public void setResultsCollected(int resultsCollected) {
        this.resultsCollected = resultsCollected;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets.clear();
        if (facets != null) {
            this.facets.addAll(facets);
        }
    }

    public Pagination getNextPagination() {
        return nextPagination;
    }

    public void setNextPagination(Pagination nextPagination) {
        this.nextPagination = nextPagination;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results.clear();
        if (results != null) {
            this.results.addAll(results);
        }
    }

    public Pagination getDefaultPagination() {
        return defaultPagination;
    }

    public void setDefaultPagination(Pagination defaultPagination) {
        this.defaultPagination = defaultPagination;
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
