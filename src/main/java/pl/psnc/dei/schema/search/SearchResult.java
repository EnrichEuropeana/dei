package pl.psnc.dei.schema.search;

public class SearchResult {
    private String imageURL;

    private String title;

    private String author;

    private String issued;

    private String provider;

    private String format;

    private String language;

    private String license;

    public SearchResult(String imageURL, String title, String author, String issued, String provider, String format, String language, String license) {
        this.imageURL = imageURL;
        this.title = title;
        this.author = author;
        this.issued = issued;
        this.provider = provider;
        this.format = format;
        this.language = language;
        this.license = license;
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
}
