package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StyleSheet("frontend://styles/styles.css")
public class FacetBox extends AccordionPanel {
    private static final Map<String, String> FACET_LABELS;

    private List<Checkbox> values;

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

    public FacetBox(String label, List<String> valueLabels) {
        setSummaryText(FACET_LABELS.get(label));
        values = new ArrayList<>();
        valueLabels.forEach(s -> {
            Checkbox checkbox = new Checkbox(s);
            values.add(checkbox);
        });
        addContent(values.toArray(new Checkbox[0]));
    }
}
