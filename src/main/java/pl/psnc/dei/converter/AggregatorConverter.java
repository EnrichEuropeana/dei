package pl.psnc.dei.converter;

import pl.psnc.dei.model.Aggregator;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AggregatorConverter implements AttributeConverter<Aggregator, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Aggregator aggregator) {
		return aggregator.getId();
	}

	@Override
	public Aggregator convertToEntityAttribute(Integer id) {
		return Aggregator.getAggregator(id);
	}
}
