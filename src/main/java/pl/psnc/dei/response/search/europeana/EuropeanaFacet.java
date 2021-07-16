package pl.psnc.dei.response.search.europeana;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import pl.psnc.dei.response.search.Facet;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "fields"
})

@Getter
@Setter
public class EuropeanaFacet implements Facet<EuropeanaFacetField> {

    @JsonProperty("name")
    private String name;
    @JsonProperty("fields")
    private List<EuropeanaFacetField> fields;

    @JsonIgnore
    @Override
    public List<String> getFieldsAsStrings() {
        List<String> strings = new ArrayList<>();
        Objects.requireNonNull(fields).forEach(field -> strings.add(field.toString()));
        return strings;
    }
}
