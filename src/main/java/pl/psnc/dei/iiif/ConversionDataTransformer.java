package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.conversion.ConversionData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class ConversionDataTransformer {

    private static final Logger logger = LoggerFactory.getLogger(ConversionDataTransformer.class);

    public static ConversionData toDBModel(ConversionDataHolder.ConversionData conversionData) {
        ConversionData dbModel = new ConversionData();
        dbModel.setId(conversionData.id);
        dbModel.setImagePath(conversionData.imagePath);
        dbModel.setJson(conversionData.json.toString());
        dbModel.setMediaType(conversionData.mediaType);
        dbModel.setSrcFilePath(
                conversionData.srcFile == null ? null : conversionData.srcFile.getAbsolutePath()
        );
        dbModel.setSrcFileUrl(
                conversionData.srcFileUrl == null ? null : conversionData.srcFileUrl.toString()
        );
        dbModel.setOutFilePath(conversionData.outFile.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
        dbModel.setDimension(conversionData.dimensions);
        return dbModel;
    }

    public static ConversionDataHolder.ConversionData toSystemModel(ConversionData conversionData) {
        ConversionDataHolder.ConversionData systemModel = new ConversionDataHolder.ConversionData();
        systemModel.id = conversionData.getId();
        systemModel.dimensions = conversionData.getDimension();
        systemModel.imagePath = conversionData.getImagePath();
        systemModel.json = JSON.parse(conversionData.getJson());
        systemModel.mediaType = conversionData.getMediaType();
        systemModel.srcFile =
                conversionData.getSrcFilePath() == null ? null : new File(conversionData.getSrcFilePath());
        try {
            systemModel.srcFileUrl = new URL(conversionData.getSrcFileUrl());
        } catch (MalformedURLException e) {
            logger.error("Incorrect file URL for conversion data id: " + conversionData.getId());
        }
        systemModel.outFile = conversionData.getOutFilePath().stream()
                .map(File::new).collect(Collectors.toList());
        return systemModel;
    }
}
