package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
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
    public static final int DEFAULT_PAGE_SIZE = 10;
    private static final List<Integer> ROWS_PER_PAGE_ALLOWED_VALUES = new ArrayList<>();
    private static final int FIRST_PAGE = 1;

    static {
        ROWS_PER_PAGE_ALLOWED_VALUES.add(DEFAULT_PAGE_SIZE);
        ROWS_PER_PAGE_ALLOWED_VALUES.add(20);
        ROWS_PER_PAGE_ALLOWED_VALUES.add(50);
        ROWS_PER_PAGE_ALLOWED_VALUES.add(100);
    }

    private int rowsPerPage = 10;

    // search results container
    private transient SearchResults searchResults;

    // results label
    private Label resultsCount;

    private Select<Integer> rowsCount;

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

        if (rowsCount == null) {
            createRowsCountSelect(DEFAULT_PAGE_SIZE);
        }
        HorizontalLayout rowsCountLayout = new HorizontalLayout();
        rowsCountLayout.addClassName("rows-per-page");
        rowsCountLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        rowsCountLayout.add(new Label("Rows per page:"), rowsCount);
        navigationBar.add(rowsCountLayout);

        // page navigation
        pageNavigationComponent = new PageNavigationComponent(this, rowsPerPage, searchResults.getTotalResults());
        navigationBar.add(pageNavigationComponent);

        add(navigationBar);
    }

    /**
     * Create component to choose number of result per page on result page.
     *
     * @param rowsPerPageToSet number of rows that should be set as active
     */
    private void createRowsCountSelect(int rowsPerPageToSet) {
        rowsCount = new Select<>();
        rowsCount.addClassName("rows-per-page-combobox");
        rowsCount.setItems(ROWS_PER_PAGE_ALLOWED_VALUES);
        rowsCount.setValue(rowsPerPageToSet);
        rowsCount.addValueChangeListener(e -> {
            rowsPerPage = e.getValue();
            searchPage.executeRowsPerPageChange(paginationCache.get(0).getRequestParams());
        });
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
        pageNavigationComponent.resetPages(rowsPerPage, searchResults.getTotalResults());
    }

    /**
     * Handles new search results
     *
     * @param searchResults result of the search
     */
    public void handleSearchResults(SearchResults searchResults) {
        this.searchResults = searchResults;

        rowsPerPage = searchResults.getResultsCollected();
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
        updateComponent(newPage);
        searchPage.fillMissingValuesAndVerifyResult(result);
    }

    /**
     * Text with info which pages out of total are shown.
     *
     * @return info string
     */
    private String prepareResultsText(int currentPage) {
        int from = 1 + (currentPage - 1) * rowsPerPage;
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
                .ifPresent(c -> ui.access(() -> c.updateMetadata(searchResult)));
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    /**
     * Sets active number of rows on result page. If given value is not allowed, default value is used instead.
     *
     * @param rowsPerPage numbers of rows that should be set
     * @return actual number of rows that was set
     */
    public int setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = SearchResultsComponent.ROWS_PER_PAGE_ALLOWED_VALUES.contains(rowsPerPage) ? rowsPerPage : DEFAULT_PAGE_SIZE;
        if (rowsCount == null) {
            createRowsCountSelect(this.rowsPerPage);
        } else {
            rowsCount.setValue(this.rowsPerPage);
        }
        return this.rowsPerPage;
    }
}
