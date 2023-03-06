package pl.psnc.dei.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.IIIFMappingService;

import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

/**
 * Converts transcription received from Transcription Platform to the Europeana Annotation
 */
@Component
public class TranscriptionConverter {

    private static final String FULL_TEXT_RESOURCE = "FullTextResource";
    private final IIIFMappingService iiifMappingService;

    @Autowired
    public TranscriptionConverter(IIIFMappingService iiifMappingService) {
        this.iiifMappingService = iiifMappingService;
    }

    public JsonObject convert(Record record, JsonObject transcription) {
        if (transcription == null ||
                transcription.get(TranscriptionFieldsNames.TEXT_NO_TAGS) == null ||
                StringUtils.isBlank(transcription.get(TranscriptionFieldsNames.TEXT_NO_TAGS).getAsString().value()))
            throw new IllegalArgumentException("Transcription object cannot be null");

        JsonObject annotation = new JsonObject();
        annotation.put(AnnotationFieldsNames.MOTIVATION, transcription.get(TranscriptionFieldsNames.MOTIVATION));
        annotation.put(AnnotationFieldsNames.BODY, prepareBodyObject(transcription));
        annotation.put(AnnotationFieldsNames.TARGET, prepareTargetObject(record, transcription));
        return annotation;
    }

    public JsonObject convertHTR(Record record, JsonObject transcription) {
        if (transcription == null ||
                transcription.get(TranscriptionFieldsNames.TRANSCRIPTION_DATA) == null ||
                StringUtils.isBlank(transcription.get(TranscriptionFieldsNames.TRANSCRIPTION_DATA).getAsString().value()))
            throw new IllegalArgumentException("HTR transcription object cannot be null");

        JsonObject annotation = new JsonObject();
        annotation.put(AnnotationFieldsNames.MOTIVATION, transcription.get(TranscriptionFieldsNames.MOTIVATION));
        annotation.put(AnnotationFieldsNames.BODY, prepareBodyObjectHTR(transcription));
        annotation.put(AnnotationFieldsNames.TARGET, prepareTargetObject(record, transcription));
        return annotation;
    }

    private JsonObject prepareTargetObject(Record record, JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        if (transcription.get(TranscriptionFieldsNames.STORY_ID) != null) {
            bodyObject.put(AnnotationFieldsNames.TARGET_SCOPE, EUROPEANA_ITEM_URL + transcription.get(TranscriptionFieldsNames.STORY_ID).getAsString().value());
        }
        String tpImageLink = extractImageLink(transcription);
        int tpOrderIndex = extractOrderIndex(transcription);
        String correctedImageLink = iiifMappingService.getSourceLink(record, tpOrderIndex, tpImageLink);
        bodyObject.put(AnnotationFieldsNames.TARGET_SOURCE, new JsonString(correctedImageLink));
        return bodyObject;
    }

    private JsonObject prepareBodyObjectHTR(JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        bodyObject.put(AnnotationFieldsNames.BODY_TYPE, FULL_TEXT_RESOURCE);
        if (transcription.get(TranscriptionFieldsNames.LANGUAGE) != null && !transcription.get(TranscriptionFieldsNames.LANGUAGE).getAsArray().isEmpty()) {
            bodyObject.put(AnnotationFieldsNames.BODY_LANGUAGE, transcription.get(TranscriptionFieldsNames.LANGUAGE).getAsArray().get(0).getAsObject().get(TranscriptionFieldsNames.CODE));
        }
        bodyObject.put(AnnotationFieldsNames.BODY_VALUE, transcription.get(TranscriptionFieldsNames.TRANSCRIPTION_DATA));
        bodyObject.put(AnnotationFieldsNames.BODY_FORMAT, "text/xml");
        bodyObject.put(AnnotationFieldsNames.BODY_RIGHTS, "http://creativecommons.org/publicdomain/zero/1.0/");
        return bodyObject;
    }

    private JsonObject prepareBodyObject(JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        bodyObject.put(AnnotationFieldsNames.BODY_TYPE, FULL_TEXT_RESOURCE);
        if (transcription.get(TranscriptionFieldsNames.LANGUAGES) != null && !transcription.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().isEmpty()) {
            bodyObject.put(AnnotationFieldsNames.BODY_LANGUAGE, transcription.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().get(0).getAsObject().get(TranscriptionFieldsNames.CODE));
        }
        bodyObject.put(AnnotationFieldsNames.BODY_VALUE, transcription.get(TranscriptionFieldsNames.TEXT_NO_TAGS));
        //TODO later we will have to use different value but for now it should always be text/plain
        bodyObject.put(AnnotationFieldsNames.BODY_FORMAT, "text/plain");
        //TODO later we will fill this field with a value retrieved from TP
        bodyObject.put(AnnotationFieldsNames.BODY_RIGHTS, "http://creativecommons.org/publicdomain/zero/1.0/");
        return bodyObject;
    }

    private int extractOrderIndex(JsonObject transcription) {
        return transcription.get(TranscriptionFieldsNames.ORDER_INDEX)
                .getAsNumber()
                .value()
                .intValue();
    }

    private String extractImageLink(JsonObject transcription) {
        return transcription.get(TranscriptionFieldsNames.IMAGE_LINK)
                .getAsString()
                .value();
    }
}
