package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.schema.search.Pagination;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.schema.search.SearchResults;
import pl.psnc.dei.ui.pages.SearchPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@StyleSheet("frontend://styles/styles.css")
public class SearchResultsComponent extends VerticalLayout {
    public static final int DEFAULT_PAGE_SIZE = 12;
    private static final int FIRST_PAGE = 1;

    // search results container
    private transient SearchResults searchResults;

    // results label
    private Label resultsCount;

    // navigation bar with results count and page navigation
    private HorizontalLayout navigationBar;

    // component with page navigation buttons
    private PageNavigationComponent pageNavigationComponent;

    // list of results
    private VerticalLayout resultsList;

    private SearchPage searchPage;

    private CurrentUserRecordSelection currentUserRecordSelection;

    private List<Pagination> paginationCache = new ArrayList<>();

    public SearchResultsComponent(SearchPage searchPage,
                                  CurrentUserRecordSelection currentUserRecordSelection) {
        this.searchPage = searchPage;
        this.currentUserRecordSelection = currentUserRecordSelection;
        this.searchResults = new SearchResults();

        addClassName("search-results-component");
        setPadding(false);
        setVisible(searchResults.getResults() != null
                && !searchResults.getResults().isEmpty());
    }

    /**
     * Create results list
     */
    private void createResultList() {
        resultsList = new VerticalLayout();
        resultsList.setPadding(false);
        add(resultsList);
    }

    /**
     * Create navigation bar with info about number of results and page navigation
     */
    private void createNavigationBar() {
        navigationBar = new HorizontalLayout();
        navigationBar.addClassName("navigation-bar");
        navigationBar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // add results count
        resultsCount = new Label(prepareResultsText(FIRST_PAGE));
        navigationBar.add(resultsCount);

        // page navigation
        pageNavigationComponent = new PageNavigationComponent(this, DEFAULT_PAGE_SIZE, searchResults.getTotalResults());
        navigationBar.add(pageNavigationComponent);

        add(navigationBar);
    }

    /**
     * Shows / hides this component according to number of results.
     */
    private void updateComponent(int currentPage) {
        setVisible(searchResults != null
                && searchResults.getResults() != null
                && !searchResults.getResults().isEmpty());
        if (isVisible()) {
            if (navigationBar == null) {
                createNavigationBar();
            } else {
                updateNavigationBar();
            }
            if (resultsList == null) {
                createResultList();
            }
            resultsCount.setText(prepareResultsText(currentPage));
            resultsList.removeAll();
            searchResults.getResults().forEach(searchResult ->
					resultsList.add(new SearchResultEntryComponent(currentUserRecordSelection, searchResult)));
        }
    }

    /**
     * Resets the page navigation component when number of total results has changed
     */
    private void updateNavigationBar() {
        pageNavigationComponent.resetPages(DEFAULT_PAGE_SIZE, searchResults.getTotalResults());
    }

    /**
     * Handles new search results
     *
     * @param searchResults result of the search
     */
    public void handleSearchResults(SearchResults searchResults) {
        this.searchResults = searchResults;

        paginationCache.clear();
        paginationCache.add(searchResults.getDefaultPagination());
        paginationCache.add(searchResults.getNextPagination());

        updateComponent(FIRST_PAGE);
    }


    /**
     * Change page from current to a new one in either direction
     *
     * @param currentPage current page
     * @param newPage     new page
     */
    void goToPage(int currentPage, int newPage) {
        int page;
        Pagination pagination = paginationCache.size() >= newPage ? paginationCache.get(newPage - 1) : null;

        SearchResults result = null;
        if (pagination == null) {
            if (newPage > currentPage) {
                pagination = paginationCache.get(paginationCache.size() - 1);
                page = currentPage;
                while (++page <= newPage) {
                    Map<String, String> paginationParams = pagination.getRequestParams();
                    result = searchPage.goToPage(paginationParams);
                    if (result == null) {
                        // redirect to error page
                        Notification.show("Search failed!", 3000, Notification.Position.MIDDLE);
                        return;
                    }
                    pagination = result.getNextPagination();
                    paginationCache.add(result.getNextPagination());
                }
            } else {
                // do nothing when the current page is requested
                return;
            }
        } else {
            Map<String, String> paginationParams = pagination.getRequestParams();
            result = searchPage.goToPage(paginationParams);
            if (result == null) {
                // redirect to error page
                Notification.show("Search failed!", 3000, Notification.Position.MIDDLE);
                return;
            }
        }

        this.searchResults = result;
        searchPage.fillMissingValuesAndVerifyResult(result);
        updateComponent(newPage);
    }

    /**
     * Text with info which pages out of total are shown.
     *
     * @return info string
     */
    private String prepareResultsText(int currentPage) {
        int from = 1 + (currentPage - 1) * DEFAULT_PAGE_SIZE;
        int to = from + searchResults.getResultsCollected() - 1;
        int of = searchResults.getTotalResults();

        return from + " - " + to + " of " + of;
    }

    public void clear() {
        searchResults.clear();
        updateComponent(FIRST_PAGE);
    }

    public void selectAll() {
        resultsList.getChildren()
				.filter(c -> c instanceof SearchResultEntryComponent)
				.map(c -> (SearchResultEntryComponent)c)
                .filter(SearchResultEntryComponent::isRecordEnabled)
				.forEach(e -> e.setRecordSelected(true));
    }

    public void deselectAll() {
        resultsList.getChildren()
                .filter(c -> c instanceof SearchResultEntryComponent)
                .map(c -> (SearchResultEntryComponent)c)
                .forEach(e -> e.setRecordSelected(false));
    }

    public void inverseSelection() {
		resultsList.getChildren()
				.filter(c -> c instanceof SearchResultEntryComponent)
				.map(c -> (SearchResultEntryComponent)c)
                .filter(SearchResultEntryComponent::isRecordEnabled)
				.forEach(SearchResultEntryComponent::invertRecordSelection);
    }

    public void updateSearchResult(UI ui, SearchResult searchResult) {
        String recordId = searchResult.getId();
        resultsList.getChildren()
                .filter(c -> c instanceof SearchResultEntryComponent)
                .map(c -> (SearchResultEntryComponent)c)
                .filter(c -> c.getRecordId().equals(recordId))
                .findFirst()
                .ifPresent(/*c -> this.getUI().ifPresent(*/c -> {
                    ui.access(() -> {
                        c.updateMetadata(searchResult);
                        ui.push(); //todo for some reason vaadin is not pushing any changes to client. try polling again?
                    });
                    //ui.push();
                }/*)*/);
    }
}
