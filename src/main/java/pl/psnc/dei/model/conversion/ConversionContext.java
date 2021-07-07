package pl.psnc.dei.model.conversion;

import org.hibernate.annotations.NaturalId;
import pl.psnc.dei.iiif.*;
import pl.psnc.dei.model.Record;

import javax.persistence.*;

@Entity
public class ConversionContext {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @OneToOne
    private Record record;

    // PROCESSING STATE STORAGE
    private boolean hasSavedFiles;
    private boolean hasConvertedToIIIF;
    private boolean hasDownloadedImage;
    private boolean hasDownloadedJson;

    // PROCESSING DATA STORAGE
    private String recordJsonRaw;
    private String recordJson;
    private String srcFileUrl;
    private String outFileUrl;
    private String imagePath;
    private String mediaType;
    private Dimension dimension;

    // new EuropeanaConversionDataHolder(record.getIdentifier(), aggregatorData.get(), recordJson, recordJsonRaw);

    public ConversionContext from(Record record) {
        ConversionContext conversionContext = new ConversionContext();
        conversionContext.setRecord(record);
        conversionContext.setHasConvertedToIIIF(false);
        conversionContext.setHasDownloadedImage(false);
        conversionContext.setHasSavedFiles(false);
        conversionContext.setRecordJson("");
        conversionContext.setRecordJsonRaw("");
        conversionContext.setSrcFileUrl("");
        conversionContext.setOutFileUrl("");
        conversionContext.setImagePath("");
        conversionContext.setMediaType("");
        conversionContext.setDimension(new Dimension(-1, -1));
        // TODO: should use repository
        return null;
    }

    // TODO: inflate and deflate to and from Europeana and Deutche...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public boolean isHasSavedFiles() {
        return hasSavedFiles;
    }

    public void setHasSavedFiles(boolean hasSavedFiles) {
        this.hasSavedFiles = hasSavedFiles;
    }

    public boolean isHasConvertedToIIIF() {
        return hasConvertedToIIIF;
    }

    public void setHasConvertedToIIIF(boolean hasConvertedToIIIF) {
        this.hasConvertedToIIIF = hasConvertedToIIIF;
    }

    public boolean isHasDownloadedImage() {
        return hasDownloadedImage;
    }

    public void setHasDownloadedImage(boolean hasDownloaded) {
        this.hasDownloadedImage = hasDownloaded;
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

    public String getSrcFileUrl() {
        return srcFileUrl;
    }

    public void setSrcFileUrl(String srcFileUrl) {
        this.srcFileUrl = srcFileUrl;
    }

    public String getOutFileUrl() {
        return outFileUrl;
    }

    public void setOutFileUrl(String outFileUrl) {
        this.outFileUrl = outFileUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public boolean isHasDownloadedJson() {
        return hasDownloadedJson;
    }

    public void setHasDownloadedJson(boolean hasDownloadedJson) {
        this.hasDownloadedJson = hasDownloadedJson;
    }
}
