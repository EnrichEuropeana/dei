package pl.psnc.dei.response.search;

import pl.psnc.dei.schema.search.Pagination;

import java.util.List;

public interface SearchResponse<T extends Facet, U extends Item> {

	Integer getItemsCount();

	void setItemsCount(Integer itemsCount);

	Integer getTotalResults();

	void setTotalResults(Integer totalResults);

	Pagination getPagination();

	void setPagination(Pagination pagination);

	Pagination getDefaultPagination();

	List<U> getItems();

	void setItems(List<U> items);

	List<T> getFacets();

	void setFacets(List<T> facets);
}
