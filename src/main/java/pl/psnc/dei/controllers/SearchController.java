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
import pl.psnc.dei.service.SearchService;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.psnc.dei.schema.search.EuropeanaCursorPagination.CURSOR_PARAM_NAME;
import static pl.psnc.dei.schema.search.EuropeanaCursorPagination.FIRST_CURSOR;
import static pl.psnc.dei.ui.components.FacetComponent.QF_PARAM_NAME;
import static pl.psnc.dei.ui.pages.SearchPage.ONLY_IIIF_PARAM_NAME;

@RestController
public class SearchController {

    private SearchService searchService;

    private static final String QUERY_ALL = "*";

    private static final String[] EUROPEANA_FIXED_PARAMS = {"query", "qf", "cursor", "only_iiif"};

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }


    @GetMapping(value = "/api/search", produces = "application/json")
    public Mono<SearchResponse> search(@RequestParam(value = "aggregator_id") Integer aggregatorId,
                                       @RequestParam(value = "query") String query,
                                       @RequestParam MultiValueMap<String, String> allParams) {
        Aggregator aggregator = Aggregator.getAggregator(aggregatorId);

        switch (aggregator) {
            case EUROPEANA:
                return handleEuropeanaSearchRequest(query, allParams);
            case DDB:
                //todo handle params for ddb
            default:
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletResponse response = ((ServletRequestAttributes)requestAttributes).getResponse();
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                }
                return Mono.empty();
        }
    }

    private Mono<SearchResponse> handleEuropeanaSearchRequest(String query, MultiValueMap<String, String> allParams) {
        String qf = null;
        String cursor;
        boolean onlyIiif;

        if (query.isEmpty()) {
            query = QUERY_ALL;
        }

        List<String> qfParam = allParams.get(QF_PARAM_NAME);
        if (!(qfParam == null || qfParam.isEmpty())) {
            qf = qfParam.get(0);
        }

        List<String> cursorParam = allParams.get(CURSOR_PARAM_NAME);
        if (cursorParam == null || cursorParam.isEmpty() || cursorParam.get(0) ==  null || cursorParam.get(0).isEmpty()) {
            cursor = FIRST_CURSOR;
        } else {
            cursor = cursorParam.get(0);
        }

        List<String> onlyIiifParam = allParams.get(ONLY_IIIF_PARAM_NAME);
        if (onlyIiifParam == null || onlyIiifParam.isEmpty()) {
            onlyIiif = true;
        } else {
            onlyIiif = Boolean.parseBoolean(onlyIiifParam.get(0));
        }

        allParams.keySet().removeAll(Arrays.asList(EUROPEANA_FIXED_PARAMS));

        Map<String, String> otherParams = new HashMap<>();
        allParams.forEach((k, v) -> {
            String joinValue = String.join(",", v);
            otherParams.put(k, joinValue);
        });

        return searchService.search(query, qf, cursor, onlyIiif, otherParams);
    }
}
