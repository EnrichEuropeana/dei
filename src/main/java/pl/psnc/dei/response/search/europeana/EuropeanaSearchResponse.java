package pl.psnc.dei.response.search.europeana;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.schema.search.EuropeanaCursorPagination;
import pl.psnc.dei.schema.search.Pagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "apikey",
        "success",
        "requestNumber",
        "itemsCount",
        "totalResults",
        "nextCursor",
        "items",
        "facets"
})
public class EuropeanaSearchResponse implements SearchResponse<EuropeanaFacet, EuropeanaItem> {

    @JsonProperty("apikey")
    private String apikey;
    @JsonProperty("success")
    private Boolean success;
    @JsonProperty("requestNumber")
    private Integer requestNumber;
    @JsonProperty("itemsCount")
    private Integer itemsCount;
    @JsonProperty("totalResults")
    private Integer totalResults;
    @JsonProperty("nextCursor")
    private String nextCursor;
    @JsonProperty("items")
    private List<EuropeanaItem> items;
    @JsonProperty("facets")
    private List<EuropeanaFacet> facets;

    @JsonProperty("apikey")
    public String getApikey() {
        return apikey;
    }

    @JsonProperty("apikey")
    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    @JsonProperty("success")
    public Boolean getSuccess() {
        return success;
    }

    @JsonProperty("success")
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @JsonProperty("requestNumber")
    public Integer getRequestNumber() {
        return requestNumber;
    }

    @JsonProperty("requestNumber")
    public void setRequestNumber(Integer requestNumber) {
        this.requestNumber = requestNumber;
    }

    @JsonProperty("itemsCount")
    @Override
    public Integer getItemsCount() {
        return itemsCount;
    }

    @JsonProperty("itemsCount")
    @Override
    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    @JsonProperty("totalResults")
    @Override
    public Integer getTotalResults() {
        return totalResults;
    }

    @JsonProperty("totalResults")
    @Override
    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    @JsonProperty("nextCursor")
    public String getNextCursor() {
        return nextCursor;
    }

    @JsonProperty("nextCursor")
    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    @JsonProperty("items")
    @Override
    public List<EuropeanaItem> getItems() {
        return items;
    }

    @JsonProperty("items")
    @Override
    public void setItems(List<EuropeanaItem> items) {
        this.items = items;
    }

    @JsonProperty("facets")
    @Override
    public List<EuropeanaFacet> getFacets() {
        return facets;
    }

    @JsonProperty("facets")
    @Override
    public void setFacets(List<EuropeanaFacet> facets) {
        this.facets = facets;
    }

    @Override
    public Pagination getPagination() {
        return new EuropeanaCursorPagination(this.nextCursor, String.valueOf(this.itemsCount));
    }

    @Override
    public void setPagination(Pagination pagination) {
        this.nextCursor = ((EuropeanaCursorPagination)pagination).getCursor();
    }

    @Override
    public Pagination getDefaultPagination() {
        return new EuropeanaCursorPagination(String.valueOf(itemsCount));
    }
}
