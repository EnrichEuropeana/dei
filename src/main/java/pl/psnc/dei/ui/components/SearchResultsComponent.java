package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.Item;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.schema.search.*;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.service.UIPollingManager;
import pl.psnc.dei.ui.pages.SearchPage;

import java.util.*;

@StyleSheet("frontend://styles/styles.css")
public class SearchResultsComponent extends VerticalLayout {
    public static final int DEFAULT_PAGE_SIZE = 12;

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

    private EuropeanaRestService europeanaRestService;

    private UIPollingManager uiPollingManager;

    private RecordTransferValidationCache recordTransferValidationCache;

    public SearchResultsComponent(SearchPage searchPage,
                                  CurrentUserRecordSelection currentUserRecordSelection,
                                  EuropeanaRestService europeanaRestService,
                                  UIPollingManager uiPollingManager,
                                  RecordTransferValidationCache recordTransferValidationCache) {
        this.searchPage = searchPage;
        this.currentUserRecordSelection = currentUserRecordSelection;
        this.europeanaRestService = europeanaRestService;
        this.uiPollingManager = uiPollingManager;
        this.recordTransferValidationCache = recordTransferValidationCache;
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
        resultsCount = new Label(prepareResultsText());
        navigationBar.add(resultsCount);

        // page navigation
        pageNavigationComponent = new PageNavigationComponent(this, DEFAULT_PAGE_SIZE, searchResults.getTotalResults());
        navigationBar.add(pageNavigationComponent);

        add(navigationBar);
    }

    /**
     * Shows / hides this component according to number of results.
     */
    private void updateComponent() {
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
            resultsCount.setText(prepareResultsText());
            resultsList.removeAll();
            searchResults.getResults().forEach(searchResult ->
					resultsList.add(new SearchResultEntryComponent(currentUserRecordSelection, europeanaRestService,
                            uiPollingManager, recordTransferValidationCache, searchResult)));
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
     * @param searchResponse result of the search
     */
    public void handleSearchResults(SearchResponse<Facet, Item> searchResponse) {
        searchResults.clear();
        recordTransferValidationCache.clear(); //todo move?

        updateSearchResults(searchResponse, true);
        updateComponent();
    }

    /**
     * Change page from current to a new one in either direction
     *
     * @param currentPage current page
     * @param newPage     new page
     */
    void goToPage(int currentPage, int newPage) {
        int page;
        if (newPage < currentPage) {
            Pagination pagination = searchResults.getPagination(newPage);
            if (pagination == null) {
                // when there is no cursor for specific page we should start from the beginning
                searchResults.setNextPagination(searchResults.getDefaultPagination());
                searchResults.setResultsCollected(0);
                page = 0;
            } else {
                // we should set all things as we would be one page behind the requested one
                searchResults.setNextPagination(pagination);
                searchResults.setResultsCollected(DEFAULT_PAGE_SIZE * (newPage - 1));
                page = newPage - 1;
            }
        } else if (newPage > currentPage) {
            // we will start from the current page and retrieve missing pages one by one
            page = currentPage;
        } else {
            // do nothing when the current page is requested
            return;
        }
        while (++page <= newPage) {
            Map<String, String> paginationParams = searchResults.getNextPagination().getRequestParams();
            SearchResponse<Facet, Item> result = searchPage.executePaginationSearch(paginationParams);
            if (result == null) {
                // redirect to error page
                Notification.show("Search failed!", 3000, Notification.Position.MIDDLE);
                return;
            }
            updateSearchResults(result, page == newPage);
        }
        updateComponent();
    }

    /**
     * Updates SearchResults container based on the SearchResponse. When updateResultsList is true also the actual result items are updated.
     *
     * @param result            result from search execution
     * @param updateResultsList when true the actual items are also updated
     */
    private void updateSearchResults(SearchResponse<Facet, Item> result, boolean updateResultsList) {
        if (result.getTotalResults() == 0) {
            searchResults.clear();
        } else {
            searchResults.setResultsCollected(searchResults.getResultsCollected() + result.getItemsCount());

            Pagination pagination = searchResults.getNextPagination() == null ? result.getDefaultPagination() : searchResults.getNextPagination();
            if (searchResults.getResultsCollected() % DEFAULT_PAGE_SIZE == 0) {
                searchResults.setPagination(searchResults.getResultsCollected() / DEFAULT_PAGE_SIZE, pagination);
            } else {
                searchResults.setPagination((searchResults.getResultsCollected() / DEFAULT_PAGE_SIZE) + 1, pagination);
            }
            searchResults.setNextPagination(result.getPagination());
            searchResults.setDefaultPagination(result.getDefaultPagination());
            searchResults.setTotalResults(result.getTotalResults());
            searchResults.setFacets(result.getFacets());
            searchResults.setResults(new ArrayList<>());

            if (updateResultsList) {
                List<Item> items = result.getItems();
                items.forEach(item -> searchResults.getResults().add(itemToSearchResult(item)));
            }
        }
    }

    /**
     * Create a SearchResult object from EuropeanaItem which is retrieved from the response
     *
     * @param item item found in the results
     * @return item converted to search result object
     */
    private SearchResult itemToSearchResult(Item item) {
        SearchResult searchResult = new SearchResult();

        //id
        searchResult.setId(item.getId());

        // title
        if (item.getTitle() != null && !item.getTitle().isEmpty()) {
            searchResult.setTitle(item.getTitle().get(0));
        }

        // author
        if (item.getAuthor() != null && !item.getAuthor().isEmpty()) {
            searchResult.setAuthor(item.getAuthor());
        }

        // issued

        // provider institution
        if (item.getDataProviderInstitution() != null && !item.getDataProviderInstitution().isEmpty()) {
            searchResult.setProvider(item.getDataProviderInstitution());
        }

        // format

        // language
        if (item.getLanguage() != null && !item.getLanguage().isEmpty()) {
            searchResult.setLanguage(item.getLanguage().get(0));
        }

        // license
        if (item.getRights() != null && !item.getRights().isEmpty()) {
            searchResult.setLicense(item.getRights().get(0));
        }

        // image URL
        if (item.getThumbnailURL() != null && !item.getThumbnailURL().isEmpty()) {
            searchResult.setImageURL(item.getThumbnailURL());
        }

        // source object URL
        if (item.getSourceObjectURL() != null && !item.getSourceObjectURL().isEmpty()) {
            searchResult.setSourceObjectURL(item.getSourceObjectURL());
        }

        return searchResult;
    }

    /**
     * Text with info which pages out of total are shown.
     *
     * @return info string
     */
    private String prepareResultsText() {
        return prepareFromResultsText()
                + " - "
                + searchResults.getResultsCollected()
                + " of "
                + searchResults.getTotalResults();
    }

    /**
     * Text with first shown result
     *
     * @return first shown result index
     */
    private String prepareFromResultsText() {
        int fromResults = searchResults.getResultsCollected();
        if (fromResults >= DEFAULT_PAGE_SIZE) {
            if (fromResults >= searchResults.getTotalResults()) {
                fromResults -= searchResults.getResults().size();
            } else {
                fromResults -= DEFAULT_PAGE_SIZE;
            }
        } else {
            fromResults = 0;
        }
        return String.valueOf(fromResults + 1);
    }

    public void clear() {
        searchResults.clear();
        updateComponent();
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
}
