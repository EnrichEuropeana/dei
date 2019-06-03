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
import pl.psnc.dei.service.search.EuropeanaSearchService;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SearchController {

    private EuropeanaSearchService europeanaSearchService;

    public SearchController(EuropeanaSearchService europeanaSearchService) {
        this.europeanaSearchService = europeanaSearchService;
    }

    @GetMapping(value = "/api/search", produces = "application/json")
    public Mono<SearchResponse> search(@RequestParam(value = "aggregator") Integer aggregatorId,
                                       @RequestParam(value = "query") String query,
                                       @RequestParam MultiValueMap<String, String> allParams) {

        Map<String, String> requestParams = new HashMap<>();
        allParams.forEach((k, v) -> {
            String joinValue = String.join(",", v);
            requestParams.put(k, joinValue);
        });

        Aggregator aggregator = Aggregator.getById(aggregatorId);

        switch (aggregator) {
            case EUROPEANA:
                return europeanaSearchService.search(query, requestParams);
            case DDB:
                //todo search in ddb
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
