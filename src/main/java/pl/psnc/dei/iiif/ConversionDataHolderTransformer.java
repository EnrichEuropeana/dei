package pl.psnc.dei.iiif;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.conversion.ConversionData;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.util.List;

/**
 * Service extracts fileObjects from concrete implementations of ConversionDataHolder and put them back there
 */
@Service
public class ConversionDataHolderTransformer {

    private final EuropeanaConversionDataHolderTransformerStrategy europeanaConversionDataTransformerStrategy = new EuropeanaConversionDataHolderTransformerStrategy();
    private final DDBConversionDataHolderTransformerStrategy ddbConversionDataTransformerStrategy = new DDBConversionDataHolderTransformerStrategy();

    public List<ConversionData> toDBModel(ConversionDataHolder conversionDataHolder) {
        if (conversionDataHolder.getClass().isAssignableFrom(EuropeanaConversionDataHolder.class)) {
            return europeanaConversionDataTransformerStrategy.toDBModel((EuropeanaConversionDataHolder) conversionDataHolder);
        }
        if (conversionDataHolder.getClass().isAssignableFrom(DDBConversionDataHolder.class)) {
            return ddbConversionDataTransformerStrategy.toDBModel((DDBConversionDataHolder) conversionDataHolder);
        }
        throw new IllegalArgumentException("Cannot convert object of class: " + conversionDataHolder.getClass());
    }

    public ConversionDataHolder toConversionDataHolder(ConversionTaskContext conversionTaskContext) throws ConversionImpossibleException {
        Aggregator aggregator = conversionTaskContext.getRecord().getAggregator();
        switch(aggregator) {
            case EUROPEANA: {
                return europeanaConversionDataTransformerStrategy.toConversionDataHolder(conversionTaskContext);
            }
            case DDB: {
                return ddbConversionDataTransformerStrategy.toConversionDataHolder(conversionTaskContext);
            }
            default:
                throw new IllegalStateException("Unsupported aggregator");
        }
    }
}
