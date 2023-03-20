package pl.psnc.dei.model;

import lombok.Data;
import org.apache.jena.atlas.json.JsonObject;

@Data
public class HTRTranscription extends AbstractTranscription {
    public HTRTranscription(Record record) {
        super(record);
    }

    public HTRTranscription(String tpId, Record record, String annotationId) {
        super(tpId, record, annotationId);
    }


    public TranscriptionType getTranscriptionType() {
        return TranscriptionType.HTR;
    }

    @Override
    public void from(JsonObject transcription) {

    }
}
