package pl.psnc.dei.model.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.model.TranscriptionType;
import pl.psnc.dei.util.AnnotationFieldsNames;
import pl.psnc.dei.util.ItemFieldsNames;
import pl.psnc.dei.util.TranscriptionConverter;
import pl.psnc.dei.util.TranscriptionFieldsNames;

import java.util.Objects;

import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

@Component
public class HTRTranscriptionFactory implements TranscriptionFactory {

    @Value("${htr.transcription.mime.type}")
    private String mimeType;

    @Override
    public Transcription createTranscription(Record record, JsonObject original, TranscriptionConverter converter) {
        JsonValue data = original.get("data");
        if (data != null && data.isArray()) {
            original = data.getAsArray().get(0).getAsObject();
        }

        Transcription transcription = new Transcription();
        transcription.setTranscriptionType(TranscriptionType.HTR);
        transcription.setRecord(record);
        transcription.setTpId(original.get("HtrDataId").toString());
        transcription.setItemId(getItemId(original));
        transcription.setTranscriptionContent(
                converter.convert(record, original, this));
        JsonValue europeanaAnnotationId = original.get("EuropeanaAnnotationId");
        if (europeanaAnnotationId != null && !"0".equals(europeanaAnnotationId.toString())) {
            transcription.setAnnotationId(europeanaAnnotationId.toString());
        }
        return transcription;
    }

    public void validateTranscription(JsonObject transcription) {
        Objects.requireNonNull(transcription.get(TranscriptionFieldsNames.TRANSCRIPTION_DATA),
                "HTR Transcription content cannot be null");
        if (StringUtils.isBlank(transcription.get(TranscriptionFieldsNames.TRANSCRIPTION_DATA).getAsString().value())) {
            throw new IllegalArgumentException("HTR Transcription content cannot be empty");
        }
    }

    public JsonObject prepareBodyObject(JsonObject transcription) {
        JsonObject bodyObject = new JsonObject();
        // body type is always FullTextResource
        bodyObject.put(AnnotationFieldsNames.BODY_TYPE, FULL_TEXT_RESOURCE);
        // language
        fillLanguage(transcription, bodyObject);
        // body values
        fillBodyValues(transcription, bodyObject);

        return bodyObject;
    }


    private void fillBodyValues(JsonObject transcription, JsonObject bodyObject) {
        bodyObject.put(AnnotationFieldsNames.BODY_VALUE,
                transcription.get(TranscriptionFieldsNames.TRANSCRIPTION_DATA));
        bodyObject.put(AnnotationFieldsNames.BODY_FORMAT, mimeType);
        fillLicense(bodyObject);
    }

    private void fillLanguage(JsonObject transcription, JsonObject bodyObject) {
        JsonValue languages = transcription.get(TranscriptionFieldsNames.LANGUAGE);
        if (languages.isArray() && !languages.getAsArray().isEmpty()) {
            bodyObject.put(AnnotationFieldsNames.BODY_LANGUAGE,
                    languages.getAsArray().get(0).getAsObject().get(TranscriptionFieldsNames.CODE));
        } else {
            throw new IllegalArgumentException("Mandatory language property is missing.");
        }
    }

    public JsonObject prepareTargetObject(Record record, JsonObject transcription, String imageLink) {
        JsonObject bodyObject = new JsonObject();
        // HTR Transcription does not have record id, get it from record object
        bodyObject.put(AnnotationFieldsNames.TARGET_SCOPE, EUROPEANA_ITEM_URL + record.getIdentifier());
        bodyObject.put(AnnotationFieldsNames.TARGET_SOURCE, new JsonString(imageLink));
        return bodyObject;
    }

    public long getItemId(JsonObject transcription) {
        return transcription.get(ItemFieldsNames.ITEM_ID).getAsNumber().value().longValue();
    }
}
