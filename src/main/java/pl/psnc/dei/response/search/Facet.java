package pl.psnc.dei.response.search;

import java.util.List;

public interface Facet<T extends FacetField> {

	String getName();

	void setName(String name);

	List<T> getFields();

	void setFields(List<T> fields);

	List<String> getFieldsAsStrings();
}
