package pl.psnc.dei.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TranscriptionTypeConverter implements AttributeConverter<Transcription.TranscriptionType, String> {
    @Override
    public String convertToDatabaseColumn(Transcription.TranscriptionType transcriptionType) {
        return transcriptionType.name();
    }

    @Override
    public Transcription.TranscriptionType convertToEntityAttribute(String s) {
        return Transcription.TranscriptionType.from(s);
    }
}
