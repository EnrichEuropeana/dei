package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import pl.psnc.dei.controllers.SearchController;
import pl.psnc.dei.schema.search.SearchResults;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.FacetComponent;
import pl.psnc.dei.ui.components.SearchResultsComponent;


@Route(value = "search", layout = MainView.class)
@UIScope
@SpringComponent
public class SearchPage extends HorizontalLayout {
    private TextField search;

    private FacetComponent facets;

    private SearchResultsComponent resultsComponent;

    public SearchPage(SearchController searchController) {
        setDefaultVerticalComponentAlignment(Alignment.STRETCH);
        setAlignSelf(Alignment.STRETCH, this);

        VerticalLayout searchResultsList = createSearchResultsList(searchController);
        createFacetComponent();
        add(facets, searchResultsList);
        expand(searchResultsList);
    }

    private void createFacetComponent() {
        facets = new FacetComponent();
        facets.setPadding(false);
    }

    private VerticalLayout createSearchResultsList(SearchController searchController) {
        VerticalLayout searchResultsList = new VerticalLayout();

        searchResultsList.add(createQueryForm());
        resultsComponent = new SearchResultsComponent(searchController);
        searchResultsList.add(resultsComponent);
        return searchResultsList;
    }

    private HorizontalLayout createQueryForm() {
        HorizontalLayout queryForm = new HorizontalLayout();
        queryForm.addClassName("query-form");
        queryForm.setPadding(false);
        queryForm.setSizeFull();

        search = new TextField();
        search.addClassName("search-field");
        search.setPlaceholder("Search in Europeana");
        search.addKeyUpListener(Key.ENTER, keyUpEvent -> executeSearch(search.getValue(), null, SearchResults.FIRST_CURSOR));

        Button searchButton = new Button();
        searchButton.setIcon(new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(e -> executeSearch(search.getValue(), null, SearchResults.FIRST_CURSOR));
        queryForm.add(search, searchButton);
        queryForm.expand(search);
        queryForm.setDefaultVerticalComponentAlignment(Alignment.START);
        queryForm.expand();
        return queryForm;
    }

    private void executeSearch(String value, String qf, String cursor) {
        SearchResults results = resultsComponent.executeSearch(value, qf, cursor);
        if (results != null) {
            facets.addFacets(results.getFacets());
        }
    }
}
