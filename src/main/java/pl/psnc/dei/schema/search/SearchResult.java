package pl.psnc.dei.schema.search;

import pl.psnc.dei.util.IiifAvailability;

public class SearchResult {

    private String id;

    private String imageURL;

    private String title;

    private String author;

    private String issued;

    private String provider;

    private String format;

    private String language;

    private String license;

    private IiifAvailability iiifAvailability;

    /**
     * URL to object on aggregator portal
     */
    private String sourceObjectURL;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSourceObjectURL() {
        return sourceObjectURL;
    }

    public void setSourceObjectURL(String sourceObjectURL) {
        this.sourceObjectURL = sourceObjectURL;
    }

    public IiifAvailability getIiifAvailability() {
        return iiifAvailability;
    }

    public void setIiifAvailability(IiifAvailability iiifAvailability) {
        this.iiifAvailability = iiifAvailability;
    }
}
