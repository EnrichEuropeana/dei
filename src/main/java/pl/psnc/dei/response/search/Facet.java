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
public class Facet {

    @JsonProperty("name")
    private String name;
    @JsonProperty("fields")
    private List<FacetField> fields = null;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("fields")
    public List<FacetField> getFields() {
        return fields;
    }

    @JsonProperty("fields")
    public void setFields(List<FacetField> fields) {
        this.fields = fields;
    }

    @JsonIgnore
    public List<String> getFieldsAsStrings() {
        List<String> strings = new ArrayList<>();
        Objects.requireNonNull(fields).forEach(field -> strings.add(field.toString()));
        return strings;
    }
}
