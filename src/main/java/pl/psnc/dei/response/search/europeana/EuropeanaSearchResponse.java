package pl.psnc.dei.response.search.europeana;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
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
