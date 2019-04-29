package pl.psnc.dei.converter;

import pl.psnc.dei.model.Record;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RecordStateConverter implements AttributeConverter<Record.RecordState, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Record.RecordState recordState) {
		return recordState.getValue();
	}

	@Override
	public Record.RecordState convertToEntityAttribute(Integer value) {
		return Record.RecordState.getState(value);
	}
}
