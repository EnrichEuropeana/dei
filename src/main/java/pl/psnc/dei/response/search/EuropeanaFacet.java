package pl.psnc.dei.response.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "fields"
})
public class EuropeanaFacet implements Facet<EuropeanaFacetField> {

    @JsonProperty("name")
    private String name;
    @JsonProperty("fields")
    private List<EuropeanaFacetField> fields = null;

    @JsonProperty("name")
    @Override
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("fields")
    @Override
    public List<EuropeanaFacetField> getFields() {
        return fields;
    }

    @JsonProperty("fields")
    @Override
    public void setFields(List<EuropeanaFacetField> fields) {
        this.fields = fields;
    }

    @JsonIgnore
    @Override
    public List<String> getFieldsAsStrings() {
        List<String> strings = new ArrayList<>();
        Objects.requireNonNull(fields).forEach(field -> strings.add(field.toString()));
        return strings;
    }
}
