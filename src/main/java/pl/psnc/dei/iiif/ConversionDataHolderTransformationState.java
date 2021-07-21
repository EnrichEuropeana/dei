package pl.psnc.dei.iiif;

import pl.psnc.dei.model.conversion.ConversionData;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.util.List;

/**
 * Define state behavioural pattern for transformation strategy for transformer
 */
public abstract class ConversionDataHolderTransformationState<T extends ConversionDataHolder> {
    public abstract ConversionDataHolder toConversionDataHolder(ConversionTaskContext conversionTaskContext) throws ConversionImpossibleException;

    public abstract List<ConversionData> toDBModel(T conversionDataHolder);
}
