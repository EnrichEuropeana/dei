package pl.psnc.dei.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.model.TranscriptionType;
import pl.psnc.dei.model.factory.TranscriptionFactory;
import pl.psnc.dei.service.IIIFMappingService;
import pl.psnc.dei.service.TranscriptionPlatformService;

import java.util.Objects;

import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

/**
 * Converts transcription received from Transcription Platform to the Europeana Annotation
 */
@Component
public class TranscriptionConverter {

    private final IIIFMappingService iiifMappingService;

    private final TranscriptionPlatformService transcriptionPlatformService;

    @Autowired
    public TranscriptionConverter(IIIFMappingService iiifMappingService, TranscriptionPlatformService transcriptionPlatformService) {
        this.iiifMappingService = iiifMappingService;
        this.transcriptionPlatformService = transcriptionPlatformService;
    }

    public JsonObject convert(Record record, JsonObject transcription, TranscriptionFactory transcriptionFactory) {
        Objects.requireNonNull(transcription, "Transcription from TP cannot be null");
        Objects.requireNonNull(record, "Record cannot be null");

        transcriptionFactory.validateTranscription(transcription);

        JsonObject annotation = new JsonObject();
        annotation.put(AnnotationFieldsNames.MOTIVATION, transcriptionFactory.getMotivation());
        annotation.put(AnnotationFieldsNames.BODY, transcriptionFactory.prepareBodyObject(transcription));
        annotation.put(AnnotationFieldsNames.TARGET, prepareTargetObject(record, transcription, transcriptionFactory));
        return annotation;
    }

    private JsonObject prepareTargetObject(Record record, JsonObject transcription, TranscriptionFactory transcriptionFactory) {
        JsonValue item = transcriptionPlatformService.fetchMetadataEnrichmentsForItem(transcriptionFactory.getItemId(transcription));
        Objects.requireNonNull(item);

        String tpImageLink = extractImageLink(item.getAsObject());
        int tpOrderIndex = extractOrderIndex(item.getAsObject());
        String correctedImageLink = iiifMappingService.getSourceLink(record, tpOrderIndex, tpImageLink);
        if (!(correctedImageLink.startsWith("https://") || correctedImageLink.startsWith("http://"))) {
            return transcriptionFactory.prepareTargetObject(record, transcription, "https://" + correctedImageLink);
        }
        return transcriptionFactory.prepareTargetObject(record, transcription, correctedImageLink);
    }

    private int extractOrderIndex(JsonObject item) {
        return item.get(TranscriptionFieldsNames.ORDER_INDEX)
                .getAsNumber()
                .value()
                .intValue();
    }

    private String extractImageLink(JsonObject item) {
        JsonValue imageLink = JSON.parse(item.get(TranscriptionFieldsNames.IMAGE_LINK)
                .getAsString()
                .value());
        JsonValue id = imageLink.getAsObject().get("@id");
        Objects.requireNonNull(id);
        return id.getAsString().value();
    }
}
