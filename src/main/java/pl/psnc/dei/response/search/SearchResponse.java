package pl.psnc.dei.response.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
public class SearchResponse {

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
    private List<Item> items = null;
    @JsonProperty("facets")
    private List<Facet> facets = null;

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
    public Integer getItemsCount() {
        return itemsCount;
    }

    @JsonProperty("itemsCount")
    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    @JsonProperty("totalResults")
    public Integer getTotalResults() {
        return totalResults;
    }

    @JsonProperty("totalResults")
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
    public List<Item> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<Item> items) {
        this.items = items;
    }

    @JsonProperty("facets")
    public List<Facet> getFacets() {
        return facets;
    }

    @JsonProperty("facets")
    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }
}
