package pl.psnc.dei.response.search.ddb;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.psnc.dei.response.search.Item;

import java.util.Date;
import java.util.List;

public class DDBItem implements Item {

	@JsonProperty("id")
	private String id;
	@JsonProperty("type")
	private String type;
	@JsonProperty("category")
	private String category;
	@JsonProperty("media")
	private String media;
	@JsonProperty("label")
	private String label;
	@JsonProperty("title")
	private List<String> title;
	@JsonProperty("subtitle")
	private String subtitle;
	@JsonProperty("preview")
	private String preview;
	@JsonProperty("view")
	private List<String> view;
	@JsonProperty("thumbnail")
	private String thumbnail;
	@JsonProperty("longitude")
	private Double longitude;
	@JsonProperty("latitude")
	private Double latitude;
	@JsonProperty("last_update")
	private Date lastUpdate;
	@JsonProperty("fulltext")
	private List<String> fullText;
	@JsonProperty("match")
	private List<String> match;
	private String author;
	private String format;
	private List<String> language;
	private List<String> rights;
	private String thumbnailUrl;
	private String dataProvider;
	private String sourceUrlObject;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public List<String> getTitle() {
		return title;
	}

	@Override
	public void setTitle(List<String> title) {
		this.title = title;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String getDataProviderInstitution() {
		return dataProvider;
	}

	@Override
	public void setDataProviderInstitution(String dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public List<String> getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(List<String> language) {
		this.language = language;
	}

	@Override
	public List<String> getRights() {
		return rights;
	}

	@Override
	public void setRights(List<String> rights) {
		this.rights = rights;
	}

	@Override
	public String getThumbnailURL() {
		return thumbnailUrl;
	}

	@Override
	public void setThumbnailURL(String edmPreview) {
		this.thumbnailUrl = edmPreview;
	}

	@Override
	public String getSourceObjectURL() {
		return sourceUrlObject;
	}

	@Override
	public void setSourceObjectURL(String guid) {
		this.sourceUrlObject = guid;
	}
}
