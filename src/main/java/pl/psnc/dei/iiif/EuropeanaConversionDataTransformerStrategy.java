package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionData;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EuropeanaConversionDataTransformerStrategy extends ConversionDataTransformationState<EuropeanaConversionDataHolder> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ConversionDataHolder toConversionDataHolder(ConversionTaskContext conversionTaskContext) throws ConversionImpossibleException {
        JsonObject recordJson = JSON.parse(conversionTaskContext.getRecordJson());
        JsonObject recordJsonRaw = JSON.parse(conversionTaskContext.getRecordJsonRaw());
        Record record = conversionTaskContext.getRecord();
        List<ConversionDataHolder.ConversionData> convertedData = conversionTaskContext.getRawConversionData().stream()
                .map(el -> {
                    ConversionDataHolder.ConversionData a = new ConversionDataHolder.ConversionData();
                    a.dimensions = el.getDimension();
                    a.imagePath = el.getImagePath();
                    a.json = JSON.parse(el.getJson());
                    a.mediaType = el.getMediaType();
                    a.srcFile = new File(el.getSrcFilePath());
                    try {
                        a.srcFileUrl = new URL(el.getSrcFileUrl());
                    } catch (MalformedURLException e) {
                        logger.error("Incorrect file URL for record: {}, url: {}", record.getIdentifier(), el.getSrcFileUrl(), e);
                    }
                    a.outFile = el.getOutFilePath().stream()
                            .map(File::new).collect(Collectors.toList());
                    return a;
                }).collect(Collectors.toList());

        Optional<JsonObject> aggregatorData = recordJson.get("@graph").getAsArray().stream()
                .map(JsonValue::getAsObject)
                .filter(e -> e.get("edm:isShownBy") != null)
                .findFirst();

        if (!aggregatorData.isPresent()) {
            throw new ConversionImpossibleException("Can't convert! Record doesn't contain files list!");
        }

        EuropeanaConversionDataHolder europeanaConversionDataHolder = new EuropeanaConversionDataHolder(record.getIdentifier(), aggregatorData.get(), recordJson, recordJsonRaw);
        europeanaConversionDataHolder.fileObjects = convertedData;
        return europeanaConversionDataHolder;
    }

    @Override
    public List<ConversionData> toDBModel(EuropeanaConversionDataHolder conversionDataHolder) {
        List<ConversionDataHolder.ConversionData> conversionData = conversionDataHolder.fileObjects;
        return conversionData.stream()
                .map(el -> {
                    ConversionData a = new ConversionData();
                    a.setImagePath(el.imagePath);
                    a.setJson(el.json.toString());
                    a.setMediaType(el.mediaType);
                    a.setSrcFilePath(el.srcFile.getAbsolutePath());
                    a.setSrcFileUrl(el.srcFileUrl.toString());
                    a.setOutFilePath(el.outFile.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
                    return a;
                }).collect(Collectors.toList());
    }
}