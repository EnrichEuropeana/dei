package pl.psnc.dei.model.conversion;

import pl.psnc.dei.iiif.*;
import pl.psnc.dei.model.Record;

import javax.persistence.*;

/**
 * Stores context for converter
 */
@Entity
public class ConversionData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recordJsonRaw;
    private String recordJson;
    private String srcFileUrl;
    private String outFileUrl;
    private String imagePath;
    private String mediaType;
    private Dimension dimension;

    // new EuropeanaConversionDataHolder(record.getIdentifier(), aggregatorData.get(), recordJson, recordJsonRaw);

    public static ConversionData from(Record record) {
        ConversionData conversionContext = new ConversionData();
        conversionContext.setRecordJson("");
        conversionContext.setRecordJsonRaw("");
        conversionContext.setSrcFileUrl("");
        conversionContext.setOutFileUrl("");
        conversionContext.setImagePath("");
        conversionContext.setMediaType("");
        conversionContext.setDimension(new Dimension(-1, -1));
        return conversionContext;
    }

    // TODO: inflate and deflate to and from Europeana and Deutche...

    public ConversionDataHolder.ConversionData inflate() {
        return null;
    }

    public void deflate(ConversionDataHolder.ConversionData conversionDataHolder) {
        this.setSrcFileUrl("");
        this.setOutFileUrl("");
        this.setImagePath("");
        this.setMediaType("");
        this.setDimension(new Dimension(-1, -1));
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
}
