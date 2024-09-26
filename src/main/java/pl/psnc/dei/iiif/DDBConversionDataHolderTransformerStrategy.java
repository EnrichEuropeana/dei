package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DDBConversionDataHolderTransformerStrategy implements ConversionDataHolderTransformationState<DDBConversionDataHolder> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ConversionDataHolder toConversionDataHolder(ConversionTaskContext conversionTaskContext) {
        JsonObject recordJson = JSON.parse(conversionTaskContext.getRecordJson());
        JsonObject recordJsonRaw = JSON.parse(conversionTaskContext.getRecordJsonRaw());
        Record record = conversionTaskContext.getRecord();

        List<ConversionDataHolder.ConversionData> convertedData = conversionTaskContext.getRawConversionData().stream()
                .map(el -> {
                    ConversionDataHolder.ConversionData conversionData = new ConversionDataHolder.ConversionData();
                    conversionData.id = el.getId();
                    conversionData.dimensions = el.getDimension();
                    conversionData.imagePath = el.getImagePath();
                    conversionData.json = JSON.parse(el.getJson());
                    conversionData.mediaType = el.getMediaType();
                    conversionData.srcFile =
                            el.getSrcFilePath() == null ? null : new File(el.getSrcFilePath());
                    try {
                        conversionData.srcFileUrl = new URL(el.getSrcFileUrl());
                    } catch (MalformedURLException e) {
                        logger.error("Incorrect file URL for record: {}, url: {}", record.getIdentifier(), el.getSrcFileUrl(), e);
                    }
                    conversionData.outFile = el.getOutFilePath().stream()
                            .map(File::new).collect(Collectors.toList());
                    return conversionData;
                }).collect(Collectors.toList());
        DDBConversionDataHolder conversionDataHolder = new DDBConversionDataHolder(record.getIdentifier(), recordJson);
        conversionDataHolder.fileObjects = convertedData;
        return conversionDataHolder;
    }
}
