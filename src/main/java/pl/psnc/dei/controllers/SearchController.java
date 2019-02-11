package pl.psnc.dei.controllers;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.service.SearchService;
import reactor.core.publisher.Mono;

@RestController
public class SearchController {

    private ApplicationContext applicationContext;

    private static final String QUERY_ALL = "*";

    public SearchController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping(value = "/api/search", produces = "application/json")
    public Mono<SearchResponse> search(@RequestParam(value = "query") String query,
                                       @RequestParam(value = "qf", required = false) String qf,
                                       @RequestParam(value = "cursor", required = false) String cursor) {
        if (query.isEmpty()) {
            query = QUERY_ALL;
        }
        SearchService searchService = applicationContext.getBean(SearchService.class);

        return searchService.search(query, qf, cursor);
    }

}
