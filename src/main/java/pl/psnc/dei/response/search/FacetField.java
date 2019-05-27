package pl.psnc.dei.response.search;

public interface FacetField {

	String getLabel();

	void setLabel(String label);

	Integer getCount();

	void setCount(Integer count);

	String toString();
}
