package pl.psnc.dei.ui.components.facets;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.psnc.dei.util.EuropeanaConstants.FACET_LABELS;

public class SelectedFacetsComponent extends HorizontalLayout {
    private FacetComponent facetComponent;

    private transient Map<String, List<String>> selectedValues;

    public SelectedFacetsComponent(FacetComponent facetComponent) {
        selectedValues = new HashMap<>();
        this.facetComponent = facetComponent;
        setSpacing(false);
        setSizeFull();
        addClassName("selected-facets");
    }

    public void addSelectedValues(String facetField, List<String> facetValues) {
        this.selectedValues.put(facetField, facetValues);
        updateLabels();
    }

    private void updateLabels() {
        removeAll();
        selectedValues.forEach((s, strings) -> strings.forEach(v -> {
            Icon icon = new Icon(VaadinIcon.CLOSE_SMALL);
            icon.addClassName("selected-facet-icon");
            Button button = new Button(v, icon);
            button.addClassName("selected-facet-button");
            button.addClickListener(buttonClickEvent -> {
                selectedValues.get(s).remove(v);
                remove(button);
                facetComponent.executeFacetSearch(s, v, false);
            });
            button.setIconAfterText(false);
            button.getElement().setAttribute("title", FACET_LABELS.get(s));
            add(button);
        }));
    }

    public void clear() {
        selectedValues.clear();
        updateLabels();
    }
}
