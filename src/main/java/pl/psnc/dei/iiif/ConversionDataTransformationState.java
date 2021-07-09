package pl.psnc.dei.iiif;

import pl.psnc.dei.model.conversion.ConversionData;

import java.util.List;

/**
 * Define state structural pattern for transformation strategy for transformer
 */
public abstract class ConversionDataTransformationState {
    public abstract ConversionDataHolder toConversionDataHolder();
    public abstract List<ConversionData> toDBModel();
}
