package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import pl.psnc.dei.controllers.SearchController;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.FacetComponent;


@Route(value = "search", layout = MainView.class)
@UIScope
@SpringComponent
public class SearchPage extends HorizontalLayout {

    private TextField search;

    private FacetComponent facets;

    private transient SearchController searchController;

    private String nextCursor = "*";

    public SearchPage(SearchController searchController) {
        this.searchController = searchController;

        setDefaultVerticalComponentAlignment(Alignment.STRETCH);

        HorizontalLayout queryForm = new HorizontalLayout();

        search = new TextField();
        search.setPlaceholder("Search in Europeana");
        search.addKeyUpListener(Key.ENTER, keyUpEvent -> executeSearch(search.getValue()));
        search.getStyle().set("max-width", "500px");

        Button searchButton = new Button();
        searchButton.setIcon(new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(e -> executeSearch(search.getValue()));

        facets = new FacetComponent();
        facets.setPadding(false);
        facets.setWidth("25%");
        facets.getStyle().set("max-width", "250px");

        add(facets);
        queryForm.add(search, searchButton);
        add(queryForm);
        expand(queryForm);
        queryForm.expand(search);
        queryForm.setDefaultVerticalComponentAlignment(Alignment.START);

        executeSearch("*");
        setAlignSelf(Alignment.STRETCH, this);
    }

    private void executeSearch(String value) {
        SearchResponse result = searchController.search(value, null, nextCursor).block();
        nextCursor = result.getNextCursor() != null ? result.getNextCursor() : "*";
        facets.addFacets(result.getFacets());
    }
}
