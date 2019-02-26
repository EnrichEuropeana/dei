package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StyleSheet("frontend://styles/styles.css")
public class FacetBox extends AccordionPanel {
    // Labels for facet fields
    public static final Map<String, String> FACET_LABELS;

    private transient Map<Checkbox, FacetField> values;

    static {
        FACET_LABELS = new HashMap<>();
        FACET_LABELS.put("YEAR", "Year");
        FACET_LABELS.put("RIGHTS", "Rights");
        FACET_LABELS.put("DATA_PROVIDER", "Data provider");
        FACET_LABELS.put("PROVIDER", "Provider");
        FACET_LABELS.put("COLOURPALETTE", "Colour palette");
        FACET_LABELS.put("COUNTRY", "Country");
        FACET_LABELS.put("LANGUAGE", "Language");
        FACET_LABELS.put("MIME_TYPE", "Mime type");
        FACET_LABELS.put("TYPE", "Type");
        FACET_LABELS.put("IMAGE_SIZE", "Image size");
        FACET_LABELS.put("SOUND_DURATION", "Sound duration");
        FACET_LABELS.put("REUSABILITY", "Reusability");
        FACET_LABELS.put("VIDEO_DURATION", "Video duration");
        FACET_LABELS.put("TEXT_FULLTEXT", "Has fulltext");
        FACET_LABELS.put("LANDINGPAGE", "Landing page");
        FACET_LABELS.put("MEDIA", "Media");
        FACET_LABELS.put("THUMBNAIL", "Thumbnail");
        FACET_LABELS.put("UGC", "UGC");
        FACET_LABELS.put("IMAGE_ASPECTRATIO", "Image aspect ratio");
        FACET_LABELS.put("IMAGE_COLOUR", "Image colour");
        FACET_LABELS.put("VIDEO_HD", "Video HD");
        FACET_LABELS.put("SOUND_HQ", "Sound HQ");
    }

    // Used facet field
    private String facet;

    // parent facet component
    private FacetComponent facetComponent;

    public FacetBox(FacetComponent parent, Facet facet) {
        this.facetComponent = parent;
        this.facet = facet.getName();
        setSummaryText(FACET_LABELS.get(facet.getName()));

        values = new HashMap<>();
        facet.getFields().forEach(facetField -> {
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
    private void handleFacetField(Checkbox fieldCheckbox) {
        facetComponent.excuteFacetSearch(facet, values.get(fieldCheckbox).getLabel(), fieldCheckbox.getValue());
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
}
