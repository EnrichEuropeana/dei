package pl.psnc.dei.service.search;

import pl.psnc.dei.response.search.SearchResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface AggregatorSearchService {

	String QUERY_ALL = "*";

	String UTF_8_ENCODING = "UTF-8";

	Mono<SearchResponse> search(String query, Map<String, String> requestParams, int rowsPerPage);
}
