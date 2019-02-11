package pl.psnc.dei.response.search;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "ugc",
        "completeness",
        "country",
        "europeanaCollectionName",
        "dcLanguage",
        "edmConceptPrefLabelLangAware",
        "edmConcept",
        "edmConceptLabel",
        "edmDatasetName",
        "title",
        "dcDescription",
        "rights",
        "edmIsShownAt",
        "dataProvider",
        "dcTitleLangAware",
        "dcTypeLangAware",
        "dcLanguageLangAware",
        "europeanaCompleteness",
        "edmPreview",
        "previewNoDistribute",
        "provider",
        "timestamp",
        "score",
        "language",
        "type",
        "guid",
        "link",
        "timestamp_created_epoch",
        "timestamp_update_epoch",
        "timestamp_created",
        "timestamp_update"
})
public class Item {

    @JsonProperty("id")
    private String id;
    @JsonProperty("ugc")
    private List<Boolean> ugc = null;
    @JsonProperty("completeness")
    private Integer completeness;
    @JsonProperty("country")
    private List<String> country = null;
    @JsonProperty("europeanaCollectionName")
    private List<String> europeanaCollectionName = null;
    @JsonProperty("dcLanguage")
    private List<String> dcLanguage = null;
    @JsonProperty("edmConceptPrefLabelLangAware")
    private Map<String,List<String>> edmConceptPrefLabelLangAware = null;
    @JsonProperty("edmConcept")
    private List<String> edmConcept = null;
    @JsonProperty("edmConceptLabel")
    private List<Map<String,String>> edmConceptLabel = null;
    @JsonProperty("edmDatasetName")
    private List<String> edmDatasetName = null;
    @JsonProperty("title")
    private List<String> title = null;
    @JsonProperty("dcDescription")
    private List<String> dcDescription = null;
    @JsonProperty("rights")
    private List<String> rights = null;
    @JsonProperty("edmIsShownAt")
    private List<String> edmIsShownAt = null;
    @JsonProperty("dataProvider")
    private List<String> dataProvider = null;
    @JsonProperty("dcTitleLangAware")
    private Map<String,List<String>> dcTitleLangAware;
    @JsonProperty("dcTypeLangAware")
    private Map<String,List<String>> dcTypeLangAware;
    @JsonProperty("dcLanguageLangAware")
    private Map<String,List<String>> dcLanguageLangAware;
    @JsonProperty("europeanaCompleteness")
    private Integer europeanaCompleteness;
    @JsonProperty("edmPreview")
    private List<String> edmPreview = null;
    @JsonProperty("previewNoDistribute")
    private Boolean previewNoDistribute;
    @JsonProperty("provider")
    private List<String> provider = null;
    @JsonProperty("timestamp")
    private Long timestamp;
    @JsonProperty("score")
    private Double score;
    @JsonProperty("language")
    private List<String> language = null;
    @JsonProperty("type")
    private String type;
    @JsonProperty("guid")
    private String guid;
    @JsonProperty("link")
    private String link;
    @JsonProperty("timestamp_created_epoch")
    private Long timestampCreatedEpoch;
    @JsonProperty("timestamp_update_epoch")
    private Long timestampUpdateEpoch;
    @JsonProperty("timestamp_created")
    private String timestampCreated;
    @JsonProperty("timestamp_update")
    private String timestampUpdate;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("ugc")
    public List<Boolean> getUgc() {
        return ugc;
    }

    @JsonProperty("ugc")
    public void setUgc(List<Boolean> ugc) {
        this.ugc = ugc;
    }

    @JsonProperty("completeness")
    public Integer getCompleteness() {
        return completeness;
    }

    @JsonProperty("completeness")
    public void setCompleteness(Integer completeness) {
        this.completeness = completeness;
    }

    @JsonProperty("country")
    public List<String> getCountry() {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(List<String> country) {
        this.country = country;
    }

    @JsonProperty("europeanaCollectionName")
    public List<String> getEuropeanaCollectionName() {
        return europeanaCollectionName;
    }

    @JsonProperty("europeanaCollectionName")
    public void setEuropeanaCollectionName(List<String> europeanaCollectionName) {
        this.europeanaCollectionName = europeanaCollectionName;
    }

    @JsonProperty("dcLanguage")
    public List<String> getDcLanguage() {
        return dcLanguage;
    }

    @JsonProperty("dcLanguage")
    public void setDcLanguage(List<String> dcLanguage) {
        this.dcLanguage = dcLanguage;
    }

    @JsonProperty("edmConceptPrefLabelLangAware")
    public Map<String,List<String>> getEdmConceptPrefLabelLangAware() {
        return edmConceptPrefLabelLangAware;
    }

    @JsonProperty("edmConceptPrefLabelLangAware")
    public void setEdmConceptPrefLabelLangAware(Map<String,List<String>> edmConceptPrefLabelLangAware) {
        this.edmConceptPrefLabelLangAware = edmConceptPrefLabelLangAware;
    }

    @JsonProperty("edmConcept")
    public List<String> getEdmConcept() {
        return edmConcept;
    }

    @JsonProperty("edmConcept")
    public void setEdmConcept(List<String> edmConcept) {
        this.edmConcept = edmConcept;
    }

    @JsonProperty("edmConceptLabel")
    public List<Map<String,String>> getEdmConceptLabel() {
        return edmConceptLabel;
    }

    @JsonProperty("edmConceptLabel")
    public void setEdmConceptLabel(List<Map<String,String>> edmConceptLabel) {
        this.edmConceptLabel = edmConceptLabel;
    }

    @JsonProperty("edmDatasetName")
    public List<String> getEdmDatasetName() {
        return edmDatasetName;
    }

    @JsonProperty("edmDatasetName")
    public void setEdmDatasetName(List<String> edmDatasetName) {
        this.edmDatasetName = edmDatasetName;
    }

    @JsonProperty("title")
    public List<String> getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(List<String> title) {
        this.title = title;
    }

    @JsonProperty("dcDescription")
    public List<String> getDcDescription() {
        return dcDescription;
    }

    @JsonProperty("dcDescription")
    public void setDcDescription(List<String> dcDescription) {
        this.dcDescription = dcDescription;
    }

    @JsonProperty("rights")
    public List<String> getRights() {
        return rights;
    }

    @JsonProperty("rights")
    public void setRights(List<String> rights) {
        this.rights = rights;
    }

    @JsonProperty("edmIsShownAt")
    public List<String> getEdmIsShownAt() {
        return edmIsShownAt;
    }

    @JsonProperty("edmIsShownAt")
    public void setEdmIsShownAt(List<String> edmIsShownAt) {
        this.edmIsShownAt = edmIsShownAt;
    }

    @JsonProperty("dataProvider")
    public List<String> getDataProvider() {
        return dataProvider;
    }

    @JsonProperty("dataProvider")
    public void setDataProvider(List<String> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @JsonProperty("dcTitleLangAware")
    public Map<String,List<String>> getDcTitleLangAware() {
        return dcTitleLangAware;
    }

    @JsonProperty("dcTitleLangAware")
    public void setDcTitleLangAware(Map<String,List<String>> dcTitleLangAware) {
        this.dcTitleLangAware = dcTitleLangAware;
    }

    @JsonProperty("dcTypeLangAware")
    public Map<String,List<String>> getDcTypeLangAware() {
        return dcTypeLangAware;
    }

    @JsonProperty("dcTypeLangAware")
    public void setDcTypeLangAware(Map<String,List<String>> dcTypeLangAware) {
        this.dcTypeLangAware = dcTypeLangAware;
    }

    @JsonProperty("dcLanguageLangAware")
    public Map<String,List<String>> getDcLanguageLangAware() {
        return dcLanguageLangAware;
    }

    @JsonProperty("dcLanguageLangAware")
    public void setDcLanguageLangAware(Map<String,List<String>> dcLanguageLangAware) {
        this.dcLanguageLangAware = dcLanguageLangAware;
    }

    @JsonProperty("europeanaCompleteness")
    public Integer getEuropeanaCompleteness() {
        return europeanaCompleteness;
    }

    @JsonProperty("europeanaCompleteness")
    public void setEuropeanaCompleteness(Integer europeanaCompleteness) {
        this.europeanaCompleteness = europeanaCompleteness;
    }

    @JsonProperty("edmPreview")
    public List<String> getEdmPreview() {
        return edmPreview;
    }

    @JsonProperty("edmPreview")
    public void setEdmPreview(List<String> edmPreview) {
        this.edmPreview = edmPreview;
    }

    @JsonProperty("previewNoDistribute")
    public Boolean getPreviewNoDistribute() {
        return previewNoDistribute;
    }

    @JsonProperty("previewNoDistribute")
    public void setPreviewNoDistribute(Boolean previewNoDistribute) {
        this.previewNoDistribute = previewNoDistribute;
    }

    @JsonProperty("provider")
    public List<String> getProvider() {
        return provider;
    }

    @JsonProperty("provider")
    public void setProvider(List<String> provider) {
        this.provider = provider;
    }

    @JsonProperty("timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("score")
    public Double getScore() {
        return score;
    }

    @JsonProperty("score")
    public void setScore(Double score) {
        this.score = score;
    }

    @JsonProperty("language")
    public List<String> getLanguage() {
        return language;
    }

    @JsonProperty("language")
    public void setLanguage(List<String> language) {
        this.language = language;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @JsonProperty("link")
    public String getLink() {
        return link;
    }

    @JsonProperty("link")
    public void setLink(String link) {
        this.link = link;
    }

    @JsonProperty("timestamp_created_epoch")
    public Long getTimestampCreatedEpoch() {
        return timestampCreatedEpoch;
    }

    @JsonProperty("timestamp_created_epoch")
    public void setTimestampCreatedEpoch(Long timestampCreatedEpoch) {
        this.timestampCreatedEpoch = timestampCreatedEpoch;
    }

    @JsonProperty("timestamp_update_epoch")
    public Long getTimestampUpdateEpoch() {
        return timestampUpdateEpoch;
    }

    @JsonProperty("timestamp_update_epoch")
    public void setTimestampUpdateEpoch(Long timestampUpdateEpoch) {
        this.timestampUpdateEpoch = timestampUpdateEpoch;
    }

    @JsonProperty("timestamp_created")
    public String getTimestampCreated() {
        return timestampCreated;
    }

    @JsonProperty("timestamp_created")
    public void setTimestampCreated(String timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    @JsonProperty("timestamp_update")
    public String getTimestampUpdate() {
        return timestampUpdate;
    }

    @JsonProperty("timestamp_update")
    public void setTimestampUpdate(String timestampUpdate) {
        this.timestampUpdate = timestampUpdate;
    }

}
