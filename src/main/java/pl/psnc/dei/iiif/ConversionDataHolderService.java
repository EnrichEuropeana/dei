package pl.psnc.dei.iiif;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.DAO.ConversionDataRepository;
import pl.psnc.dei.model.conversion.ConversionData;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversionDataHolderService {
    @Autowired
    private ConversionDataRepository conversionDataRepository;

    public ConversionDataHolder save(ConversionDataHolder conversionDataHolder, ConversionTaskContext conversionTaskContext) {
        List<ConversionDataHolder.ConversionData> systemModel = conversionDataHolder.fileObjects.stream()
                .map(el -> {
                    ConversionData dbModel = ConversionDataTransformer.toDBModel(el);
                    dbModel.setConversionTaskContext(conversionTaskContext);
                    dbModel = this.conversionDataRepository.save(dbModel);
                    return ConversionDataTransformer.toSystemModel(dbModel);
                }).collect(Collectors.toList());
        conversionDataHolder.fileObjects = systemModel;
        return conversionDataHolder;
    }
}
