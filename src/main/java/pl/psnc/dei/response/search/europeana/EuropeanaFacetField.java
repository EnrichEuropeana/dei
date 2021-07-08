package pl.psnc.dei.response.search.europeana;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import pl.psnc.dei.response.search.FacetField;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "label",
        "count"
})
@Getter
@Setter
public class EuropeanaFacetField implements FacetField {

    @JsonProperty("label")
    private String label;
    @JsonProperty("count")
    private Integer count;

    @JsonIgnore
    @Override
    public String toString() {
        return String.format("%s (%d)", label, count);
    }
}
