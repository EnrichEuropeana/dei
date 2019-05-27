package pl.psnc.dei.response.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "label",
        "count"
})
public class EuropeanaFacetField implements FacetField {

    @JsonProperty("label")
    private String label;
    @JsonProperty("count")
    private Integer count;

    @JsonProperty("label")
    @Override
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("count")
    @Override
    public Integer getCount() {
        return count;
    }

    @JsonProperty("count")
    @Override
    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return String.format("%s (%d)", label, count);
    }
}
