package pl.psnc.dei.schema.search;

import pl.psnc.dei.response.search.Facet;

import java.util.ArrayList;
import java.util.List;

public class SearchResults {
    private int totalResults;

    private int resultsCollected;

    private List<Facet> facets = new ArrayList<>();

    private List<Pagination> paginations = new ArrayList<>();

    private Pagination nextPagination;

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

    public void setPagination(int page, Pagination pagination) {
        if (page <= paginations.size()) {
            paginations.set(page - 1, pagination);
        } else if (page == paginations.size() + 1) {
            paginations.add(pagination);
        }
    }

    public Pagination getPagination(int page) {
        if (page <= paginations.size()) {
            return paginations.get(page - 1);
        }
        return null;
    }

    public void clearPagination() {
        paginations.clear();
    }

    public void clear() {
        clearPagination();
        setResultsCollected(0);
        results.clear();
        facets.clear();
        setNextPagination(null);
        setTotalResults(0);
    }
}
