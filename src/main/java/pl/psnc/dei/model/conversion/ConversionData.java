package pl.psnc.dei.model.conversion;

import pl.psnc.dei.iiif.*;
import pl.psnc.dei.model.Record;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores context for converter
 */
@Entity
public class ConversionData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String json;
    private String srcFileUrl;
    private String srcFilePath;
    @ElementCollection
    private List<String> outFilePath;
    @ElementCollection
    private List<String> imagePath;
    private String mediaType;
    @ElementCollection
    private List<Dimension> dimension;

    // new EuropeanaConversionDataHolder(record.getIdentifier(), aggregatorData.get(), recordJson, recordJsonRaw);

    public static ConversionData from(Record record) {
        ConversionData conversionContext = new ConversionData();
        conversionContext.setJson("");
        conversionContext.setSrcFileUrl("");
        conversionContext.setOutFilePath("");
        conversionContext.setImagePath(new ArrayList<>());
        conversionContext.setMediaType("");
        conversionContext.setDimension(new ArrayList<>());
        conversionContext.setSrcFilePath("");
        return conversionContext;
    }

    // TODO: inflate and deflate to and from Europeana and Deutche...

    public ConversionDataHolder.ConversionData inflate() {
        return null;
    }

    public void deflate(ConversionDataHolder.ConversionData conversionDataHolder) {
        this.setSrcFileUrl("");
        this.setOutFilePath("");
        this.setImagePath(new ArrayList<>());
        this.setMediaType("");
        this.setDimension(new ArrayList<>());
    }
    public String getJson() {
        return json;
    }

    public void setJson(String recordJson) {
        this.json = recordJson;
    }

    public String getSrcFileUrl() {
        return srcFileUrl;
    }

    public void setSrcFileUrl(String srcFileUrl) {
        this.srcFileUrl = srcFileUrl;
    }

    public List<String> getOutFilePath() {
        return outFilePath;
    }

    public void setOutFilePath(List<String> outFileUrl) {
        this.outFilePath = outFileUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public List<Dimension> getDimension() {
        return dimension;
    }

    public void setDimension(List<Dimension> dimension) {
        this.dimension = dimension;
    }

    public List<String> getImagePath() {
        return imagePath;
    }

    public void setImagePath(List<String> imagePath) {
        this.imagePath = imagePath;
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public void setSrcFilePath(String srcFilePath) {
        this.srcFilePath = srcFilePath;
    }
}
