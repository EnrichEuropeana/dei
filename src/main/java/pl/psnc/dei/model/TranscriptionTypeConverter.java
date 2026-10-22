package pl.psnc.dei.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TranscriptionTypeConverter implements AttributeConverter<TranscriptionType, String> {
    @Override
    public String convertToDatabaseColumn(TranscriptionType transcriptionType) {
        return transcriptionType.name();
    }

    @Override
    public TranscriptionType convertToEntityAttribute(String s) {
        return TranscriptionType.from(s);
    }
}
