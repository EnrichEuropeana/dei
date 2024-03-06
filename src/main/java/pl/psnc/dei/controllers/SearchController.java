package pl.psnc.dei.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.service.search.DDBSearchService;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SearchController {

    private EuropeanaSearchService europeanaSearchService;
    private DDBSearchService ddbSearchService;

    public SearchController(EuropeanaSearchService europeanaSearchService) {
        this.europeanaSearchService = europeanaSearchService;
    }

    /**
     * Fetches data about given records matching query
     * @param aggregatorId id of aggregator to fetch from
     * @param query query to ask aggregator
     * @param rows number of rows to fetch
     * @param allParams other params
     * @return all rows matching query
     */
    @GetMapping(value = "/api/search", produces = "application/json")
    public Mono<SearchResponse> search(@RequestParam(value = "aggregator") Integer aggregatorId,
                                       @RequestParam(value = "query") String query,
                                       @RequestParam(value = "rows", defaultValue = "10") Integer rows,
                                       @RequestParam MultiValueMap<String, String> allParams) {

        Map<String, String> requestParams = new HashMap<>();
        allParams.forEach((k, v) -> {
            String joinValue = String.join(",", v);
            requestParams.put(k, joinValue);
        });

        Aggregator aggregator = Aggregator.getById(aggregatorId);

        switch (aggregator) {
            case EUROPEANA:
                return europeanaSearchService.search(query, requestParams, rows);
            case DDB:
                return ddbSearchService.search(query, requestParams, rows);
            case UNKNOWN:
            default:
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletResponse response = ((ServletRequestAttributes)requestAttributes).getResponse();
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                }
                return Mono.empty();
        }
    }
}
