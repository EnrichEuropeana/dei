package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Record;

import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

@Component
public class CallToActionBuilder {

    @Value("${transcribathon.url}")
    private String transcribathonLocation;

    private static final String CONTEXT_VALUE = "http://www.w3.org/ns/anno.jsonld";

    private static final String ANNOTATION_VALUE = "Annotation";

    private static final String MOTIVATION_VALUE = "linkForContributing";

    private static final String TRANSCRIBATHON_STORY_TEMPLATE = "%s/documents/story/?story=%d";

    private static final String EUROPEANA_ITEM_URL_TEMPLATE = "%s%s";


    public JsonObject fromRecord(Record record) {
        JsonObject annotation = new JsonObject();
        annotation.put(AnnotationFieldsNames.CONTEXT, CONTEXT_VALUE);
        annotation.put(AnnotationFieldsNames.BODY_TYPE, ANNOTATION_VALUE);
        annotation.put(AnnotationFieldsNames.MOTIVATION, MOTIVATION_VALUE);
        annotation.put(AnnotationFieldsNames.BODY, String.format(TRANSCRIBATHON_STORY_TEMPLATE, transcribathonLocation, record.getStoryId()));
        annotation.put(AnnotationFieldsNames.TARGET, String.format(EUROPEANA_ITEM_URL_TEMPLATE, EUROPEANA_ITEM_URL, record.getIdentifier()));
        return annotation;
    }
}
