package pl.psnc.dei.response.search.ddb;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.psnc.dei.response.search.FacetField;

public class DDBFacetField implements FacetField {

	@JsonProperty("value")
	private String label;
	@JsonProperty("count")
	private int count;

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Integer getCount() {
		return count;
	}

	@Override
	public void setCount(Integer count) {
		this.count = count;
	}

	@Override
	public String toString(){
		return String.format("%s (%d)", label, count);
	}
}
