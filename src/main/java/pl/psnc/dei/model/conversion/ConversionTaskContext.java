package pl.psnc.dei.model.conversion;

import pl.psnc.dei.iiif.ConversionDataHolder;
import pl.psnc.dei.iiif.ConversionDataHolderTransformer;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.model.Record;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores context for conversion task
 * For convenience entity automatically converts list of persistable ConversionData to and from ConversionDataHolder,
 * thus end user sees entity as able to persist ConversionDataHolder even it is not
 */
@Entity
public class ConversionTaskContext extends Context{
    // PROCESSING STATE STORAGE
    private boolean hasJson;
    private boolean hasConverted;
    private boolean hasThrownException;
    private boolean hasAddedFailure;

    // CONVERTER STATE STORAGE
    private boolean hasConverterSavedFiles;
    private boolean hasConverterConvertedToIIIF;
    private boolean hasConverterDownloadedImage;
    private boolean hasConverterDownloadedJson;

    // PROCESSING DATA STORAGE
    private String recordJsonRaw;
    private String recordJson;
    private Exception exception;

    @OneToMany
    private List<ConversionData> conversionDataHolder;

    @Transient
    private final ConversionDataHolderTransformer conversionDataHolderTransformer = new ConversionDataHolderTransformer();


    public static ConversionTaskContext from(Record record){
        ConversionTaskContext context = new ConversionTaskContext();
        context.setRecord(record);
        context.setHasJson(false);
        context.setHasConverted(false);
        context.setHasThrownException(false);
        context.setHasAddedFailure(false);
        context.setRecordJson("");
        context.setRecordJsonRaw("");
        context.setException(null);
        context.setHasAddedFailure(false);
        context.setHasConverterConvertedToIIIF(false);
        context.setHasConverterSavedFiles(false);
        context.setHasConverterDownloadedJson(false);
        context.setHasConverterDownloadedImage(false);
        context.conversionDataHolder = new ArrayList<>();
        return context;
    }

    public ConversionDataHolder getConversionDataHolder() throws ConversionImpossibleException {
       return this.conversionDataHolderTransformer.toConversionDataHolder(this);
    }

    public void setConversionDataHolder(@NotNull ConversionDataHolder conversionDataHolder) {
        this.conversionDataHolder = this.conversionDataHolderTransformer.toDBModel(conversionDataHolder);
    }

    public boolean isHasConverterSavedFiles() {
        return hasConverterSavedFiles;
    }

    public void setHasConverterSavedFiles(boolean hasConverterSavedFiles) {
        this.hasConverterSavedFiles = hasConverterSavedFiles;
    }

    public boolean isHasConverterConvertedToIIIF() {
        return hasConverterConvertedToIIIF;
    }

    public void setHasConverterConvertedToIIIF(boolean hasConverterConvertedToIIIF) {
        this.hasConverterConvertedToIIIF = hasConverterConvertedToIIIF;
    }

    public boolean isHasConverterDownloadedImage() {
        return hasConverterDownloadedImage;
    }

    public void setHasConverterDownloadedImage(boolean hasConverterDownloadedImage) {
        this.hasConverterDownloadedImage = hasConverterDownloadedImage;
    }

    public boolean isHasConverterDownloadedJson() {
        return hasConverterDownloadedJson;
    }

    public void setHasConverterDownloadedJson(boolean hasConverterDownloadedJson) {
        this.hasConverterDownloadedJson = hasConverterDownloadedJson;
    }

    public boolean isHasJson() {
        return hasJson;
    }

    public void setHasJson(boolean hasJson) {
        this.hasJson = hasJson;
    }

    public boolean isHasConverted() {
        return hasConverted;
    }

    public void setHasConverted(boolean hasConverted) {
        this.hasConverted = hasConverted;
    }

    public boolean isHasThrownException() {
        return hasThrownException;
    }

    public void setHasThrownException(boolean hasThrownException) {
        this.hasThrownException = hasThrownException;
    }

    public boolean isHasAddedFailure() {
        return hasAddedFailure;
    }

    public void setHasAddedFailure(boolean hasAddedFailure) {
        this.hasAddedFailure = hasAddedFailure;
    }

    public String getRecordJsonRaw() {
        return recordJsonRaw;
    }

    public void setRecordJsonRaw(String recordJsonRaw) {
        this.recordJsonRaw = recordJsonRaw;
    }

    public String getRecordJson() {
        return recordJson;
    }

    public void setRecordJson(String recordJson) {
        this.recordJson = recordJson;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public List<ConversionData> getRawConversionData() {
        return conversionDataHolder;
    }
}
