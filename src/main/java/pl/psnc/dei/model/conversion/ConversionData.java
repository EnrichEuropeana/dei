package pl.psnc.dei.model.conversion;

import pl.psnc.dei.iiif.Dimension;

import javax.persistence.*;
import java.util.List;

/**
 * Stores context for converter
 */
@Entity
@Table(name = "conversion_data")
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

    @ManyToOne
    private ConversionTaskContext conversionTaskContext;

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

    public ConversionTaskContext getConversionTaskContext() {
        return conversionTaskContext;
    }

    public void setConversionTaskContext(ConversionTaskContext conversionDataHolder) {
        this.conversionTaskContext = conversionDataHolder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
