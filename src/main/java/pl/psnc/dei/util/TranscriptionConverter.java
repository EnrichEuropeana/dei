package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;

import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

/**
 * Converts transcription received from Transcription Platform to the Europeana Annotation
 */
public class TranscriptionConverter {

    private static final String FULL_TEXT_RESOURCE = "FullTextResource";

    public static JsonObject convert(JsonObject transcription) {
        if (transcription == null ||
                transcription.get(TranscriptionFieldsNames.TEXT_NO_TAGS) == null ||
                transcription.get(TranscriptionFieldsNames.TEXT_NO_TAGS).getAsString().value().isEmpty())
            throw new IllegalArgumentException("Transcription object cannot be null");

        JsonObject annotation = new JsonObject();
        annotation.put(AnnotationFieldsNames.MOTIVATION, transcription.get(TranscriptionFieldsNames.MOTIVATION));
        annotation.put(AnnotationFieldsNames.BODY, prepareBodyObject(transcription));
        annotation.put(AnnotationFieldsNames.TARGET, prepareTargetObject(transcription));
        return annotation;
    }

    private static JsonObject prepareTargetObject(JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        if (transcription.get(TranscriptionFieldsNames.STORY_ID) != null) {
            bodyObject.put(AnnotationFieldsNames.TARGET_SCOPE, EUROPEANA_ITEM_URL + transcription.get(TranscriptionFieldsNames.STORY_ID).getAsString().value());
        }
        bodyObject.put(AnnotationFieldsNames.TARGET_SOURCE, transcription.get(TranscriptionFieldsNames.IMAGE_LINK));
        return bodyObject;
    }

    private static JsonObject prepareBodyObject(JsonObject transcription) {
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
}
