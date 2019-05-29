package pl.psnc.dei.service;

import pl.psnc.dei.response.search.SearchResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface AggregatorSearchService {

	String QUERY_ALL = "*";

	Mono<SearchResponse> search(String query, Map<String, String> requestParams);
}
