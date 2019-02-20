package pl.psnc.dei.schema.search;

import pl.psnc.dei.response.search.Facet;

import java.util.ArrayList;
import java.util.List;

public class SearchResults {
    public static final String FIRST_CURSOR = "*";

    private int totalResults;

    private int resultsCollected;

    private List<Facet> facets = null;

    private List<String> pageCursors = new ArrayList<>();

    private String nextCursor;

    private List<SearchResult> results = null;

    public SearchResults() {
        totalResults = 0;
        resultsCollected = 0;
        pageCursors.add(FIRST_CURSOR);
        nextCursor = FIRST_CURSOR;
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
        this.facets = facets;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    public void setPageCursor(int page, String cursor) {
        if (page <= pageCursors.size()) {
            pageCursors.set(page - 1, cursor);
        } else if (page == pageCursors.size() + 1) {
            pageCursors.add(cursor);
        }
    }

    public String getPageCursor(int page) {
        if (page <= pageCursors.size()) {
            return pageCursors.get(page - 1);
        }
        return null;
    }

    public void clearPageCursors() {
        pageCursors.clear();
        pageCursors.add(FIRST_CURSOR);
    }
}
