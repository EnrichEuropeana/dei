package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.response.search.Facet;

import java.util.*;
import java.util.stream.Collectors;

@StyleSheet("frontend://styles/styles.css")
public class FacetComponent extends VerticalLayout {
    // Expandable facets
    private Accordion facetAccordion;

    // Single facet component list
    private List<FacetBox> facetBoxes;

    // Component for showing the selected facets
    private SelectedFacetsComponent selectedFacetsComponent;

    // Search results component
    private SearchResultsComponent searchResultsComponent;

    // Filter query from facets
    Map<String, List<String>> fq = new HashMap<>();

    public FacetComponent(SearchResultsComponent resultsComponent) {
        addClassName("facet-component");
        this.searchResultsComponent = resultsComponent;
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
    public void excuteFacetSearch(String facet, String facetValue, boolean add) {
        if (add) {
            fq.computeIfAbsent(facet, k -> new ArrayList<>()).add(facetValue);
        } else {
            if (fq.containsKey(facet)) {
                fq.get(facet).remove(facetValue);
                if (fq.get(facet).isEmpty()) {
                    fq.remove(facet);
                }
            }
        }
        searchResultsComponent.executeFacetSearch(prepareQueryFilter());
    }

    /**
     * Prepare query filter where all selected values for the same facet are joined with OR and selections from different facets
     * are joined with AND
     *
     * @return query filter string like this: ((f1:v1) OR (f1:v2) ... OR (f1:vN)) AND ((f2:v1) OR (f2:v2) ... OR (f2:vN)) ... AND ((fM:v1) OR (fM:v2) ... OR (fM:vN))
     */
    private String prepareQueryFilter() {
        List<String> queryValues = new ArrayList<>();

        fq.forEach((s, strings) -> {
            String queryValue = strings.size() == 1
                    ? s + ":" + "\"" + strings.get(0) + "\""
                    : strings.stream().map(v -> "(" + s + ":" + "\"" + v + "\"" + ")").collect(Collectors.joining(" OR "));
            queryValues.add(queryValue);
        });
        if (queryValues.size() == 1) {
            return queryValues.get(0);
        }
        return queryValues.stream().map(s -> "(" + s + ")").collect(Collectors.joining(" AND "));
    }

    /**
     * Select the necessary facet value checkboxes based on the filter query
     *
     * @param qf query filter
     */
    public void updateState(String qf) {
        if (qf == null || qf.isEmpty()) {
            fq.clear();
            selectedFacetsComponent.clear();
            return;
        }

        fq.clear();

        List<String> filterQueries = Arrays.asList(qf.split(" AND "));
        filterQueries.stream().map(String::trim).map(s -> removeTrailing(s, "(")).
                map(s -> removeTrailing(s, ")")).forEach(s -> {
            List<String> filterValues = Arrays.asList(s.split(" OR "));
            filterValues.stream().map(String::trim).map(f -> removeTrailing(f, "(")).
                    map(f -> removeTrailing(f, ")")).forEach(f -> {
                String[] facetValue = f.split(":");
                fq.computeIfAbsent(facetValue[0], v -> new ArrayList<>()).add(removeTrailing(facetValue[1], "\""));
            });
        });
        if (!fq.isEmpty()) {
            fq.keySet().forEach(s -> {
                facetBoxes.stream().filter(facetBox -> facetBox.getFacet().equals(s)).forEach(facetBox -> facetBox.updateFacets(fq.get(s)));
                selectedFacetsComponent.addSelectedValues(s, fq.get(s));
            });
        }
    }

    /**
     * Helper method for removing the trailing brackets
     *
     * @param filterQuery query to be changed
     * @return the given filter query without trailing brackets
     */
    private String removeTrailing(String filterQuery, String toRemove) {
        if (filterQuery.startsWith(toRemove)) {
            filterQuery = filterQuery.substring(toRemove.length());
        }
        if (filterQuery.endsWith(toRemove)) {
            filterQuery = filterQuery.substring(0, filterQuery.length() - toRemove.length());
        }
        return filterQuery;
    }
}
