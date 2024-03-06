package pl.psnc.dei.ui.components.facets;

import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StyleSheet("./styles/styles.css")
public abstract class FacetBox extends AccordionPanel {

    protected transient Map<Checkbox, FacetField> values;

    // Used facet field
    protected String facet;

    // parent facet component
    protected FacetComponent facetComponent;

    public FacetBox(FacetComponent parent, Facet<FacetField> facet) {
        this.facetComponent = parent;
        this.facet = facet.getName();
        setSummaryText(getFacetLabelText());

        values = new HashMap<>();
        List<FacetField> facetFields = facet.getFields();
        facetFields.forEach(facetField -> {
            Checkbox checkbox = new Checkbox(facetField.toString(), checkboxBooleanComponentValueChangeEvent -> {
                if (checkboxBooleanComponentValueChangeEvent.isFromClient()) {
                    handleFacetField(checkboxBooleanComponentValueChangeEvent.getSource());
                }
            });
            values.put(checkbox, facetField);
            addContent(checkbox);
        });
    }

    /**
     * Execute facet search after clicking certain checkbox
     * @param fieldCheckbox clicked checkbox
     */
    protected void handleFacetField(Checkbox fieldCheckbox) {
        facetComponent.executeFacetSearch(facet, values.get(fieldCheckbox).getLabel(), fieldCheckbox.getValue());
    }

    /**
     * Return facet field which can be used for facet search
     * @return facet field
     */
    public String getFacet() {
        return facet;
    }

    /**
     * Update facet checkboxes based on the selected values
     * @param selectedValues list of values that should be selected
     */
    public void updateFacets(List<String> selectedValues) {
        if (selectedValues != null && !selectedValues.isEmpty()) {
            values.forEach((key, value) -> {
                if (selectedValues.contains(value.getLabel())) {
                    key.setValue(true);
                }
            });
        }
    }

    protected abstract String getFacetLabelText();
}
