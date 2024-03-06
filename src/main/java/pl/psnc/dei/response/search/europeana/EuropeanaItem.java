package pl.psnc.dei.response.search.europeana;

import com.fasterxml.jackson.annotation.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import pl.psnc.dei.response.search.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@Getter
@Setter
public class EuropeanaItem implements Item {

    @JsonProperty("id")
    private String id;
    @JsonProperty("ugc")
    private List<Boolean> ugc;
    @JsonProperty("completeness")
    private Integer completeness;
    @JsonProperty("country")
    private List<String> country;
    @JsonProperty("europeanaCollectionName")
    private List<String> europeanaCollectionName;
    @JsonProperty("edmConceptPrefLabelLangAware")
    private Map<String,List<String>> edmConceptPrefLabelLangAware;
    @JsonProperty("edmPlaceAltLabelLangAware")
    private Map<String,List<String>> edmPlaceAltLabelLangAware;
    @JsonProperty("dcDescriptionLangAware")
    private Map<String,List<String>> dcDescriptionLangAware;
    @JsonProperty("dcSubjectLangAware")
    private Map<String,List<String>> dcSubjectLangAware;
    @JsonProperty("dcTypeLangAware")
    private Map<String,List<String>> dcTypeLangAware;
    @JsonProperty("edmIsShownBy")
    private List<String> edmIsShownBy;
    @JsonProperty("dcDescription")
    private List<String> dcDescription;
    @JsonProperty("edmConcept")
    private List<String> edmConcept;
    @JsonProperty("edmConceptLabel")
    private List<Map<String,String>> edmConceptLabel;
    @JsonProperty("title")
    private List<String> title;
    @JsonProperty("rights")
    private List<String> rights;
    @JsonProperty("edmIsShownAt")
    private List<String> edmIsShownAt;
    @JsonProperty("dctermsSpatial")
    private List<String> dctermsSpatial;
    @JsonProperty("dataProvider")
    private List<String> dataProvider;
    @JsonProperty("dcTitleLangAware")
    private Map<String,List<String>> dcTitleLangAware;
    @JsonProperty("dcCreatorLangAware")
    private Map<String,List<String>> dcCreatorLangAware;
    @JsonProperty("dcContributorLangAware")
    private Map<String,List<String>> dcContributorLangAware;
    @JsonProperty("europeanaCompleteness")
    private Integer europeanaCompleteness;
    @JsonProperty("edmPlace")
    private List<String> edmPlace;
    @JsonProperty("edmPlaceLabel")
    private List<Map<String,String>> edmPlaceLabel;
    @JsonProperty("edmPlaceLatitude")
    private List<String> edmPlaceLatitude;
    @JsonProperty("edmPlaceLongitude")
    private List<String> edmPlaceLongitude;
    @JsonProperty("dcCreator")
    private List<String> dcCreator;
    @JsonProperty("dcContributor")
    private List<String> dcContributor;
    @JsonProperty("edmPreview")
    private List<String> edmPreview;
    @JsonProperty("edmPlaceLabelLangAware")
    private Map<String,List<String>> edmPlaceLabelLangAware;
    @JsonProperty("previewNoDistribute")
    private Boolean previewNoDistribute;
    @JsonProperty("provider")
    private List<String> provider;
    @JsonProperty("timestamp")
    private Long timestamp;
    @JsonProperty("score")
    private Double score;
    @JsonProperty("language")
    private List<String> language;
    @JsonProperty("type")
    private String type;
    @JsonProperty("edmDatasetName")
    private List<String> edmDatasetName;
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
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String getAuthor() {
        if (this.dcCreator != null && !this.dcCreator.isEmpty()) {
            return this.dcCreator.get(0);
        } else if (this.dcContributor != null && !this.dcContributor.isEmpty()) {
            return this.dcContributor.get(0);
        }
        return "";
    }

    @Override
    public void setAuthor(String author) {
        this.dcCreator.add(author);
    }

    @Override
    public String getDataProviderInstitution() {
        return this.dataProvider != null ? this.dataProvider.get(0) : null;
    }

    @Override
    public void setDataProviderInstitution(String dataProvider) {
        this.dataProvider.add(dataProvider);
    }

    @Override
    public String getFormat() {
        return null;//no matching fields
    }

    @Override
    public void setFormat(String format) {
        //no matching fields
    }

    @Override
    public String getThumbnailURL() {
        return this.edmPreview != null ? this.edmPreview.get(0) : null;
    }

    @Override
    public void setThumbnailURL(String edmPreview) {
        this.edmPreview.add(edmPreview);
    }

    @Override
    public String getSourceObjectURL() {
        return this.guid;
    }

    @Override
    public void setSourceObjectURL(String guid) {
        this.guid = guid;
    }
}
