package pl.psnc.dei.converter;

import pl.psnc.dei.model.enrichments.MetadataEnrichment;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class MetadataEnrichmentStateConverter implements AttributeConverter<MetadataEnrichment.EnrichmentState, Integer> {

	@Override
	public Integer convertToDatabaseColumn(MetadataEnrichment.EnrichmentState enrichmentState) {
		if(enrichmentState == null)
			return MetadataEnrichment.EnrichmentState.PENDING.getValue();
		return enrichmentState.getValue();
	}

	@Override
	public MetadataEnrichment.EnrichmentState convertToEntityAttribute(Integer value) {
		return MetadataEnrichment.EnrichmentState.getState(value);
	}
}
