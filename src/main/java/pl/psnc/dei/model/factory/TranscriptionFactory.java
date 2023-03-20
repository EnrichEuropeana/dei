package pl.psnc.dei.model.factory;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.AnnotationFieldsNames;
import pl.psnc.dei.util.TranscriptionConverter;

@Component
public interface TranscriptionFactory {
    String FULL_TEXT_RESOURCE = "FullTextResource";

    String MOTIVATION = "transcribing";

    Transcription createTranscription(Record record, JsonObject original, TranscriptionConverter converter);

    void validateTranscription(JsonObject transcription);

    JsonObject prepareBodyObject(JsonObject transcription);

    JsonObject prepareTargetObject(Record record, JsonObject transcription, String imageLink);

    long getItemId(JsonObject transcription);

    default void fillLicense(JsonObject bodyObject) {
        bodyObject.put(AnnotationFieldsNames.BODY_RIGHTS, "http://creativecommons.org/publicdomain/zero/1.0/");
    }

    default String getMotivation() {
        return MOTIVATION;
    }
}
