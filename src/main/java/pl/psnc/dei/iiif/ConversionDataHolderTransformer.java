package pl.psnc.dei.iiif;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.conversion.ConversionData;

import java.util.List;

/**
 * Service extracts fileObjects from concrete implementations of ConversionDataHolder and put them back there
 */
@Service
public class ConversionDataHolderTransformer {

    public List<ConversionData> toDBModel(ConversionDataHolder conversionDataHolder) {
        return null;
    }

    public ConversionDataHolder toConversionDataHolder() {
        return null;
    }
}
