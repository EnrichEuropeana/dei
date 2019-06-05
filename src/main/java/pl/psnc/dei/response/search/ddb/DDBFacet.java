package pl.psnc.dei.response.search.ddb;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.psnc.dei.response.search.Facet;

import java.util.ArrayList;
import java.util.List;

public class DDBFacet implements Facet<DDBFacetField> {

	@JsonProperty("field")
	private String name;
	@JsonProperty("facetValues")
	private List<DDBFacetField> fields;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<DDBFacetField> getFields() {
		return fields;
	}

	@Override
	public void setFields(List<DDBFacetField> fields) {
		this.fields = fields;
	}

	@Override
	public List<String> getFieldsAsStrings() {
		List<String> fieldsAsString = new ArrayList<>();
		for(DDBFacetField field: fields) {
			fieldsAsString.add(field.toString());
		}
		return fieldsAsString;
	}
}
