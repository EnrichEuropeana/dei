package pl.psnc.dei.ui.components.facets;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.ui.pages.SearchPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@StyleSheet("frontend://styles/styles.css")
public abstract class FacetComponent extends VerticalLayout {
    // Expandable facets
    private Accordion facetAccordion;

    // Single facet component list
    protected List<FacetBox> facetBoxes;

    // Component for showing the selected facets
    protected SelectedFacetsComponent selectedFacetsComponent;

    // Search page
    protected SearchPage searchPage;

    public FacetComponent(SearchPage searchPage) {
        addClassName("facet-component");
        this.searchPage = searchPage;
        facetAccordion = new Accordion();
        facetBoxes = new ArrayList<>();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        setSizeFull();
        setSizeUndefined();

        Label refine = new Label("Refine your query");
        refine.addClassName("refine-query-label");
        selectedFacetsComponent = new SelectedFacetsComponent(this);

        add(refine, selectedFacetsComponent, facetAccordion);
    }

    /**
     * Add facet boxes based on the list of facets
     * @param facets list of facets retrieved from the search response
     */
    public void addFacets(List<Facet> facets) {
        facetBoxes.forEach(facetBox -> facetAccordion.remove(facetBox));
        facetBoxes.clear();
        if (facets != null) {
            facets.forEach(facet -> {
                FacetBox facetBox = new FacetBox(this, facet);
                facetBoxes.add(facetBox);
                facetAccordion.add(facetBox);
            });
        }
        facetAccordion.close();
    }

    /**
     * Execute search after a facet value had been clicked
     *
     * @param facet facet field
     * @param facetValue facet value
     * @param add indicator whether the value was selected or deselected
     */
    public abstract void executeFacetSearch(String facet, String facetValue, boolean add);

    /**
     * Select the necessary facet value checkboxes based on the request parameters
     *
     * @param requestParams request parameters
     */
    public abstract void updateState(Map<String, String> requestParams);

    /**
     * Get default facets for new search request
     *
     * @return Collection of default facets for search
     */
    public abstract Map<String, String> getDefaultFacets();
}
