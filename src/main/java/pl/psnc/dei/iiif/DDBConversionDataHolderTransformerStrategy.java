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
                    ConversionDataHolder.ConversionData a = new ConversionDataHolder.ConversionData();
                    a.id = el.getId();
                    a.dimensions = el.getDimension();
                    a.imagePath = el.getImagePath();
                    a.json = JSON.parse(el.getJson());
                    a.mediaType = el.getMediaType();
                    a.srcFile =
                            el.getSrcFilePath() == null ? null : new File(el.getSrcFilePath());
                    try {
                        a.srcFileUrl = new URL(el.getSrcFileUrl());
                    } catch (MalformedURLException e) {
                        logger.error("Incorrect file URL for record: {}, url: {}", record.getIdentifier(), el.getSrcFileUrl(), e);
                    }
                    a.outFile = el.getOutFilePath().stream()
                            .map(File::new).collect(Collectors.toList());
                    return a;
                }).collect(Collectors.toList());
        DDBConversionDataHolder conversionDataHolder = new DDBConversionDataHolder(record.getIdentifier(), recordJson);
        conversionDataHolder.fileObjects = convertedData;
        return conversionDataHolder;
    }
}
