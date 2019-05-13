package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;

/**
 * Converts transcription received from Transcription Platform to the Europeana Annotation
 */
public class TranscriptionConverter {

    public static JsonObject convert(JsonObject transcription) {
        if (transcription == null)
            throw new IllegalArgumentException("Transcription object cannot be null");

        JsonObject annotation = new JsonObject();
        annotation.put(AnnotationFieldsNames.MOTIVATION, transcription.get(TranscriptionFieldsNames.MOTIVATION));
        annotation.put(AnnotationFieldsNames.GENERATOR, prepareGeneratorObject(transcription));
        annotation.put(AnnotationFieldsNames.BODY, prepareBodyObject(transcription));
        annotation.put(AnnotationFieldsNames.TARGET, prepareTargetObject(transcription));
        return annotation;
    }

    private static JsonObject prepareGeneratorObject(JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        bodyObject.put(AnnotationFieldsNames.GENERATOR_NAME, "sample");
        bodyObject.put(AnnotationFieldsNames.GENERATOR_TYPE, "sample");
        bodyObject.put(AnnotationFieldsNames.GENERATOR_HOMEPAGE, "sample");
        return bodyObject;
    }

    private static JsonObject prepareTargetObject(JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        bodyObject.put(AnnotationFieldsNames.TARGET_SCOPE, transcription.get(TranscriptionFieldsNames.STORY_ID));
        bodyObject.put(AnnotationFieldsNames.TARGET_SOURCE, transcription.get(TranscriptionFieldsNames.ITEM_ID));
        return bodyObject;
    }

    private static JsonObject prepareBodyObject(JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        bodyObject.put(AnnotationFieldsNames.BODY_ID, transcription.get(TranscriptionFieldsNames.TEXT));
        bodyObject.put(AnnotationFieldsNames.BODY_LANGUAGE, "pl");
        bodyObject.put(AnnotationFieldsNames.BODY_FORMAT, "text/html");
        return bodyObject;
    }
}
