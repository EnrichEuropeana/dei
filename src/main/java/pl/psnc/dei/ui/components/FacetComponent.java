package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.response.search.Facet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FacetComponent extends VerticalLayout {

    private List<FacetBox> facetBoxes;

    public FacetComponent() {
        facetBoxes = new ArrayList<>();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        setSizeFull();
        getStyle().set("margin-top", "0px");
        getStyle().set("padding", "16px");
        getStyle().set("background-color","lightgrey");

        H4 refine = new H4("Refine query");
        refine.getStyle().set("margin-top", "10px");
        add(refine);
    }

    public void addFacets(List<Facet> facets) {
        updateFacetBoxes(facets);
    }

    private void updateFacetBoxes(List<Facet> facets) {
        remove(facetBoxes.toArray(new FacetBox[0]));
        facetBoxes.clear();
        if (facets != null) {
            facets.forEach(facet -> facetBoxes.add(new FacetBox(facet.getName(), facet.getFieldsAsStrings())));
            add(facetBoxes.toArray(new FacetBox[0]));
        }
    }
}
