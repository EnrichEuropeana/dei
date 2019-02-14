package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.response.search.Facet;

import java.util.ArrayList;
import java.util.List;

@StyleSheet("frontend://styles/styles.css")
public class FacetComponent extends VerticalLayout {
    private Accordion facetAccordion;

    private List<FacetBox> facetBoxes;

    public FacetComponent() {
        addClassName("facet-component");
        facetAccordion = new Accordion();
        facetBoxes = new ArrayList<>();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        setSizeFull();
        setSizeUndefined();

        Label refine = new Label("Refine query");
        refine.addClassName("refine-query-label");
        add(refine, facetAccordion);
    }
    public void addFacets(List<Facet> facets) {
        facetBoxes.forEach(facetBox -> facetAccordion.remove(facetBox));
        facetBoxes.clear();
        if (facets != null) {
            facets.forEach(facet -> {
                FacetBox facetBox = new FacetBox(facet.getName(), facet.getFieldsAsStrings());
                facetBoxes.add(facetBox);
                facetAccordion.add(facetBox);
            });
        }
        facetAccordion.close();
    }
}
