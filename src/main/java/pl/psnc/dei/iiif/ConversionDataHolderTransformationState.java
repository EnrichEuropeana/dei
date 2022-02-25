package pl.psnc.dei.iiif;

import pl.psnc.dei.model.conversion.ConversionData;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Define state behavioural pattern for transformation strategy for transformer
 */
public interface ConversionDataHolderTransformationState<T extends ConversionDataHolder> {
    ConversionDataHolder toConversionDataHolder(ConversionTaskContext conversionTaskContext) throws ConversionImpossibleException;

    default List<ConversionData> toDBModel(T conversionDataHolder) {
        List<ConversionDataHolder.ConversionData> conversionData = conversionDataHolder.fileObjects;
        return conversionData.stream()
                .map(el -> {
                    ConversionData a = new ConversionData();
                    a.setId(el.id);
                    a.setImagePath(el.imagePath);
                    a.setJson(el.json.toString());
                    a.setMediaType(el.mediaType);
                    a.setSrcFilePath(el.srcFile.getAbsolutePath());
                    a.setSrcFilePath(
                            el.srcFile == null ? null : el.srcFile.getAbsolutePath()
                    );
                    a.setOutFilePath(el.outFile.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
                    a.setDimension(el.dimensions);
                    return a;
                }).collect(Collectors.toList());
    }
}
