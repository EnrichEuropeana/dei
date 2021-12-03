package pl.psnc.dei.enrichments.types;

import lombok.Data;

@Data
public class Transcription {

    private String content;

    public static Transcription fromModelTranscription(pl.psnc.dei.model.Transcription modelTranscription) {
        Transcription transcription = new Transcription();
        transcription.setContent(modelTranscription.getTranscriptionContent().toString());
        return transcription;
    }
}
