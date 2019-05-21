package pl.psnc.dei.controllers;

import org.springframework.context.ApplicationContext;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.schema.search.SearchResults;
import pl.psnc.dei.service.SearchService;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SearchController {

    private ApplicationContext applicationContext;

    private static final String QUERY_ALL = "*";

    private static final String[] FIXED_PARAMS = {"query", "qf", "cursor", "only_iiif"};

    public SearchController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping(value = "/api/search", produces = "application/json")
    public Mono<SearchResponse> search(@RequestParam(value = "query") String query,
                                       @RequestParam(value = "qf", required = false) String qf,
                                       @RequestParam(value = "cursor", required = false) String cursor,
                                       @RequestParam(value = "only_iiif", required = false) Boolean onlyIiif,
                                       @RequestParam MultiValueMap<String, String> allParams) {
        if (query.isEmpty()) {
            query = QUERY_ALL;
        }
        if (cursor ==  null || cursor.isEmpty()) {
            cursor = SearchResults.FIRST_CURSOR;
        }
        if (onlyIiif == null) {
            onlyIiif = true;
        }

        allParams.keySet().removeAll(Arrays.asList(FIXED_PARAMS));

        Map<String, String> otherParams = new HashMap<>();
        allParams.forEach((k, v) -> {
            String joinValue = String.join(",", v);
            otherParams.put(k, joinValue);
        });

        SearchService searchService = applicationContext.getBean(SearchService.class);

        return searchService.search(query, qf, cursor, onlyIiif, otherParams);
    }

}
