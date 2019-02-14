package pl.psnc.dei.response.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
        "edmConceptPrefLabelLangAware",
        "edmPlaceAltLabelLangAware",
        "dcDescriptionLangAware",
        "dcSubjectLangAware",
        "dcTypeLangAware",
        "edmIsShownBy",
        "dcDescription",
        "edmConcept",
        "edmConceptLabel",
        "title",
        "rights",
        "edmIsShownAt",
        "dctermsSpatial",
        "dataProvider",
        "dcTitleLangAware",
        "dcCreatorLangAware",
        "dcContributorLangAware",
        "europeanaCompleteness",
        "edmPlace",
        "edmPlaceLabel",
        "edmPlaceLatitude",
        "edmPlaceLongitude",
        "dcCreator",
        "dcContributor",
        "edmPreview",
        "edmPlaceLabelLangAware",
        "previewNoDistribute",
        "provider",
        "timestamp",
        "score",
        "language",
        "type",
        "edmDatasetName",
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
    @JsonProperty("edmConceptPrefLabelLangAware")
    private Map<String,List<String>> edmConceptPrefLabelLangAware = null;
    @JsonProperty("edmPlaceAltLabelLangAware")
    private Map<String,List<String>> edmPlaceAltLabelLangAware;
    @JsonProperty("dcDescriptionLangAware")
    private Map<String,List<String>> dcDescriptionLangAware;
    @JsonProperty("dcSubjectLangAware")
    private Map<String,List<String>> dcSubjectLangAware;
    @JsonProperty("dcTypeLangAware")
    private Map<String,List<String>> dcTypeLangAware;
    @JsonProperty("edmIsShownBy")
    private List<String> edmIsShownBy = null;
    @JsonProperty("dcDescription")
    private List<String> dcDescription = null;
    @JsonProperty("edmConcept")
    private List<String> edmConcept = null;
    @JsonProperty("edmConceptLabel")
    private List<Map<String,String>> edmConceptLabel = null;
    @JsonProperty("title")
    private List<String> title = null;
    @JsonProperty("rights")
    private List<String> rights = null;
    @JsonProperty("edmIsShownAt")
    private List<String> edmIsShownAt = null;
    @JsonProperty("dctermsSpatial")
    private List<String> dctermsSpatial = null;
    @JsonProperty("dataProvider")
    private List<String> dataProvider = null;
    @JsonProperty("dcTitleLangAware")
    private Map<String,List<String>> dcTitleLangAware;
    @JsonProperty("dcCreatorLangAware")
    private Map<String,List<String>> dcCreatorLangAware;
    @JsonProperty("dcContributorLangAware")
    private Map<String,List<String>> dcContributorLangAware;
    @JsonProperty("europeanaCompleteness")
    private Integer europeanaCompleteness;
    @JsonProperty("edmPlace")
    private List<String> edmPlace = null;
    @JsonProperty("edmPlaceLabel")
    private List<Map<String,String>> edmPlaceLabel = null;
    @JsonProperty("edmPlaceLatitude")
    private List<String> edmPlaceLatitude = null;
    @JsonProperty("edmPlaceLongitude")
    private List<String> edmPlaceLongitude = null;
    @JsonProperty("dcCreator")
    private List<String> dcCreator = null;
    @JsonProperty("dcContributor")
    private List<String> dcContributor = null;
    @JsonProperty("edmPreview")
    private List<String> edmPreview = null;
    @JsonProperty("edmPlaceLabelLangAware")
    private Map<String,List<String>> edmPlaceLabelLangAware;
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
    @JsonProperty("edmDatasetName")
    private List<String> edmDatasetName = null;
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
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    @JsonProperty("edmConceptPrefLabelLangAware")
    public Map<String,List<String>> getEdmConceptPrefLabelLangAware() {
        return edmConceptPrefLabelLangAware;
    }

    @JsonProperty("edmConceptPrefLabelLangAware")
    public void setEdmConceptPrefLabelLangAware(Map<String,List<String>> edmConceptPrefLabelLangAware) {
        this.edmConceptPrefLabelLangAware = edmConceptPrefLabelLangAware;
    }

    @JsonProperty("edmPlaceAltLabelLangAware")
    public Map<String,List<String>> getEdmPlaceAltLabelLangAware() {
        return edmPlaceAltLabelLangAware;
    }

    @JsonProperty("edmPlaceAltLabelLangAware")
    public void setEdmPlaceAltLabelLangAware(Map<String,List<String>> edmPlaceAltLabelLangAware) {
        this.edmPlaceAltLabelLangAware = edmPlaceAltLabelLangAware;
    }

    @JsonProperty("dcDescriptionLangAware")
    public Map<String,List<String>> getDcDescriptionLangAware() {
        return dcDescriptionLangAware;
    }

    @JsonProperty("dcDescriptionLangAware")
    public void setDcDescriptionLangAware(Map<String,List<String>> dcDescriptionLangAware) {
        this.dcDescriptionLangAware = dcDescriptionLangAware;
    }

    @JsonProperty("dcSubjectLangAware")
    public Map<String,List<String>> getDcSubjectLangAware() {
        return dcSubjectLangAware;
    }

    @JsonProperty("dcSubjectLangAware")
    public void setDcSubjectLangAware(Map<String,List<String>> dcSubjectLangAware) {
        this.dcSubjectLangAware = dcSubjectLangAware;
    }

    @JsonProperty("dcTypeLangAware")
    public Map<String,List<String>> getDcTypeLangAware() {
        return dcTypeLangAware;
    }

    @JsonProperty("dcTypeLangAware")
    public void setDcTypeLangAware(Map<String,List<String>> dcTypeLangAware) {
        this.dcTypeLangAware = dcTypeLangAware;
    }

    @JsonProperty("edmIsShownBy")
    public List<String> getEdmIsShownBy() {
        return edmIsShownBy;
    }

    @JsonProperty("edmIsShownBy")
    public void setEdmIsShownBy(List<String> edmIsShownBy) {
        this.edmIsShownBy = edmIsShownBy;
    }

    @JsonProperty("dcDescription")
    public List<String> getDcDescription() {
        return dcDescription;
    }

    @JsonProperty("dcDescription")
    public void setDcDescription(List<String> dcDescription) {
        this.dcDescription = dcDescription;
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

    @JsonProperty("title")
    public List<String> getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(List<String> title) {
        this.title = title;
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

    @JsonProperty("dctermsSpatial")
    public List<String> getDctermsSpatial() {
        return dctermsSpatial;
    }

    @JsonProperty("dctermsSpatial")
    public void setDctermsSpatial(List<String> dctermsSpatial) {
        this.dctermsSpatial = dctermsSpatial;
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

    @JsonProperty("dcCreatorLangAware")
    public Map<String,List<String>> getDcCreatorLangAware() {
        return dcCreatorLangAware;
    }

    @JsonProperty("dcCreatorLangAware")
    public void setDcCreatorLangAware(Map<String,List<String>> dcCreatorLangAware) {
        this.dcCreatorLangAware = dcCreatorLangAware;
    }

    @JsonProperty("dcContributorLangAware")
    public Map<String,List<String>> getDcContributorLangAware() {
        return dcContributorLangAware;
    }

    @JsonProperty("dcContributorLangAware")
    public void setDcContributorLangAware(Map<String,List<String>> dcContributorLangAware) {
        this.dcContributorLangAware = dcContributorLangAware;
    }

    @JsonProperty("europeanaCompleteness")
    public Integer getEuropeanaCompleteness() {
        return europeanaCompleteness;
    }

    @JsonProperty("europeanaCompleteness")
    public void setEuropeanaCompleteness(Integer europeanaCompleteness) {
        this.europeanaCompleteness = europeanaCompleteness;
    }

    @JsonProperty("edmPlace")
    public List<String> getEdmPlace() {
        return edmPlace;
    }

    @JsonProperty("edmPlace")
    public void setEdmPlace(List<String> edmPlace) {
        this.edmPlace = edmPlace;
    }

    @JsonProperty("edmPlaceLabel")
    public List<Map<String,String>> getEdmPlaceLabel() {
        return edmPlaceLabel;
    }

    @JsonProperty("edmPlaceLabel")
    public void setEdmPlaceLabel(List<Map<String,String>> edmPlaceLabel) {
        this.edmPlaceLabel = edmPlaceLabel;
    }

    @JsonProperty("edmPlaceLatitude")
    public List<String> getEdmPlaceLatitude() {
        return edmPlaceLatitude;
    }

    @JsonProperty("edmPlaceLatitude")
    public void setEdmPlaceLatitude(List<String> edmPlaceLatitude) {
        this.edmPlaceLatitude = edmPlaceLatitude;
    }

    @JsonProperty("edmPlaceLongitude")
    public List<String> getEdmPlaceLongitude() {
        return edmPlaceLongitude;
    }

    @JsonProperty("edmPlaceLongitude")
    public void setEdmPlaceLongitude(List<String> edmPlaceLongitude) {
        this.edmPlaceLongitude = edmPlaceLongitude;
    }

    @JsonProperty("dcCreator")
    public List<String> getDcCreator() {
        return dcCreator;
    }

    @JsonProperty("dcCreator")
    public void setDcCreator(List<String> dcCreator) {
        this.dcCreator = dcCreator;
    }

    @JsonProperty("dcContributor")
    public List<String> getDcContributor() {
        return dcContributor;
    }

    @JsonProperty("dcContributor")
    public void setDcContributor(List<String> dcContributor) {
        this.dcContributor = dcContributor;
    }

    @JsonProperty("edmPreview")
    public List<String> getEdmPreview() {
        return edmPreview;
    }

    @JsonProperty("edmPreview")
    public void setEdmPreview(List<String> edmPreview) {
        this.edmPreview = edmPreview;
    }

    @JsonProperty("edmPlaceLabelLangAware")
    public Map<String,List<String>> getEdmPlaceLabelLangAware() {
        return edmPlaceLabelLangAware;
    }

    @JsonProperty("edmPlaceLabelLangAware")
    public void setEdmPlaceLabelLangAware(Map<String,List<String>> edmPlaceLabelLangAware) {
        this.edmPlaceLabelLangAware = edmPlaceLabelLangAware;
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

    @JsonProperty("edmDatasetName")
    public List<String> getEdmDatasetName() {
        return edmDatasetName;
    }

    @JsonProperty("edmDatasetName")
    public void setEdmDatasetName(List<String> edmDatasetName) {
        this.edmDatasetName = edmDatasetName;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}