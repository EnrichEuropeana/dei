package pl.psnc.dei.controllers;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.dei.service.SearchService;

@RestController
public class SearchController {

    private ApplicationContext applicationContext;

    private static final String QUERY_ALL = "*";

    public SearchController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping(value = "/search", produces = "application/json")
    public String search(@RequestParam(value = "query") String query, @RequestParam(value = "qf", required = false) String qf) {
        if (query.isEmpty()) {
            query = QUERY_ALL;
        }
        SearchService searchService = applicationContext.getBean(SearchService.class);

        return searchService.search(query, qf);
    }

}
