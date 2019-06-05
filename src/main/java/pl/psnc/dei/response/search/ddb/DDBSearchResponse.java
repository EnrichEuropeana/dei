package pl.psnc.dei.response.search.ddb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.schema.search.DDBOffsetPagination;
import pl.psnc.dei.schema.search.Pagination;

import java.util.List;

public class DDBSearchResponse implements SearchResponse<DDBFacet, DDBItem> {

	private int numberOfResults;
	private List<DDBItem> items;
	private List<DDBFacet> facets;
	private Pagination pagination;

	@Override
	public Integer getItemsCount() {
		return items.size();
	}

	@Override
	public void setItemsCount(Integer itemsCount) {

	}

	@Override
	@JsonProperty("totalResults")
	public Integer getTotalResults() {
		return numberOfResults;
	}

	@Override
	@JsonProperty("numberOfResults")
	public void setTotalResults(Integer totalResults) {
		this.numberOfResults = totalResults;
	}

	@Override
	public Pagination getPagination() {
		return pagination;
	}

	@Override
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	@Override
	public Pagination getDefaultPagination() {
		return new DDBOffsetPagination();
	}

	@Override
	@JsonProperty("items")
	public List<DDBItem> getItems() {
		return items;
	}

	@Override
	@JsonDeserialize(using = ResultsDeserializer.class)
	@JsonProperty(value = "results")
	public void setItems(List<DDBItem> items) {
		this.items = items;
	}

	@Override
	@JsonProperty("facets")
	public List<DDBFacet> getFacets() {
		return facets;
	}

	@Override
	@JsonProperty("facets")
	public void setFacets(List<DDBFacet> facets) {
		this.facets = facets;
	}
}
