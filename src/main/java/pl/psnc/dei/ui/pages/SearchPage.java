package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import pl.psnc.dei.controllers.SearchController;
import pl.psnc.dei.response.search.Item;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.schema.search.SearchResults;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.FacetComponent;
import pl.psnc.dei.ui.components.SearchResultsComponent;

import java.util.ArrayList;


@Route(value = "search", layout = MainView.class)
@UIScope
@SpringComponent
public class SearchPage extends HorizontalLayout {

    private transient SearchController searchController;

    private FacetComponent facets;

    private SearchResultsComponent resultsComponent;

    private transient SearchResults searchResults;

    public SearchPage(SearchController searchController) {
        this.searchController = searchController;
        searchResults = new SearchResults();

        setDefaultVerticalComponentAlignment(Alignment.STRETCH);
        setAlignSelf(Alignment.STRETCH, this);

        VerticalLayout searchResultsList = createSearchResultsList();
        createFacetComponent();
        add(facets, searchResultsList);
        expand(searchResultsList);
    }

    private void createFacetComponent() {
        facets = new FacetComponent();
        facets.setPadding(false);
    }

    private VerticalLayout createSearchResultsList() {
        VerticalLayout searchResultsList = new VerticalLayout();

        searchResultsList.add(createQueryForm());
        resultsComponent = new SearchResultsComponent(searchResults);
        searchResultsList.add(resultsComponent);
        return searchResultsList;
    }

    private HorizontalLayout createQueryForm() {
        HorizontalLayout queryForm = new HorizontalLayout();
        queryForm.addClassName("query-form");
        queryForm.setPadding(false);
        queryForm.setSizeFull();

        TextField search = new TextField();
        search.addClassName("search-field");
        search.setPlaceholder("Search in Europeana");
        search.addKeyUpListener(Key.ENTER, keyUpEvent -> executeSearch(search.getValue(), true));

        Button searchButton = new Button();
        searchButton.setIcon(new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(e -> executeSearch(search.getValue(), true));
        queryForm.add(search, searchButton);
        queryForm.expand(search);
        queryForm.setDefaultVerticalComponentAlignment(Alignment.START);
        queryForm.expand();
        return queryForm;
    }

    private void executeSearch(String value, boolean updateResults) {
        SearchResponse result = searchController.search(value, null, SearchResults.FIRST_CURSOR).block();
        if (result == null) {
            // we should show failure warning
            Notification.show("Search failed!", 3, Notification.Position.MIDDLE);
            return;
        }
        facets.addFacets(result.getFacets());
        if (updateResults) {
            resultsComponent.setSearchResults(updateSearchResults(result));
        }
    }

    private SearchResults updateSearchResults(SearchResponse result) {
        if (result.getTotalResults() == 0) {
            searchResults.setResultsCollected(0);
            searchResults.setNextCursor(SearchResults.FIRST_CURSOR);
            searchResults.setTotalResults(result.getTotalResults());
            searchResults.setFacets(new ArrayList<>());
            searchResults.setResults(new ArrayList<>());
        } else {
            searchResults.setResultsCollected(result.getItemsCount());
            searchResults.setNextCursor(result.getNextCursor());
            searchResults.setTotalResults(result.getTotalResults());
            searchResults.setFacets(result.getFacets());
            searchResults.setResults(new ArrayList<>());

            result.getItems().forEach(item -> {
                searchResults.getResults().add(itemToSearchResult(item));
            });
        }
        return searchResults;
    }

    private SearchResult itemToSearchResult(Item item) {
        SearchResult searchResult = new SearchResult();

        // title
        if (item.getTitle() != null && !item.getTitle().isEmpty()) {
            searchResult.setTitle(item.getTitle().get(0));
        }

        // author
        if (item.getDcCreator() != null && !item.getDcCreator().isEmpty()) {
            searchResult.setAuthor(item.getDcCreator().get(0));
        } else if (item.getDcContributor() != null && !item.getDcContributor().isEmpty()) {
            searchResult.setAuthor(item.getDcContributor().get(0));
        }

        // issued

        // provider institution
        if (item.getDataProvider() != null && !item.getDataProvider().isEmpty()) {
            searchResult.setProvider(item.getDataProvider().get(0));
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
        if (item.getEdmPreview() != null && !item.getEdmPreview().isEmpty()) {
            searchResult.setImageURL(item.getEdmPreview().get(0));
        }

        return searchResult;
    }
}
