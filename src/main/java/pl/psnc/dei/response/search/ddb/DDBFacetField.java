package pl.psnc.dei.response.search.ddb;

import pl.psnc.dei.response.search.FacetField;

public class DDBFacetField implements FacetField {

	private String label;
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
		return "";
	}
}
