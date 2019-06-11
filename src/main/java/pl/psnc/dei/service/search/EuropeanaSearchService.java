package pl.psnc.dei.service.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.response.search.europeana.EuropeanaSearchResponse;
import pl.psnc.dei.response.search.SearchResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;

import static pl.psnc.dei.ui.components.facets.EuropeanaFacetComponent.FACET_SEPARATOR;
import static pl.psnc.dei.ui.pages.SearchPage.ONLY_IIIF_PARAM_NAME;
import static pl.psnc.dei.util.EuropeanaConstants.*;

@Service
public class EuropeanaSearchService extends RestRequestExecutor implements AggregatorSearchService {

    private static final String UTF_8_ENCODING = "UTF-8";

    private static final Logger log = LoggerFactory.getLogger(EuropeanaSearchService.class);

    @Value("${api.key}")
    private String apiKey;

    @Value("${search.api.url}")
    private String searchApiUrl;

    @Value("#{'${search.api.predefined.parameters}'.split(',')}")
    private List<String> searchApiPredefinedParameters;

    @Value("${search.api.iiif.query}")
    private String searchApiIiifQuery;

    public EuropeanaSearchService(WebClient.Builder webClientBuilder) {
        configure(webClientBuilder);
    }

    @PostConstruct
    private void configure() {
        setRootUri(searchApiUrl);
        log.info("Will use {} url.", searchApiUrl);
    }

    /**
     * Execute search request. Note that cursor parameter must be URL encoded.
	 *
     * @param query query string
     * @param queryFilter query filter
     * @param cursor cursor for next page of values
     * @param onlyIiif true to query only objects available via IIIF, false otherwise
     * @param otherParams other request parameters e.g. media, reusability
     * @return response from search API associated with web client
     */
    public Mono<SearchResponse> search(String query, List<String> queryFilter, String cursor, boolean onlyIiif, Map<String, String> otherParams) {
        checkParameters(query, cursor);
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam(API_KEY_PARAM_NAME, apiKey);
                    searchApiPredefinedParameters.forEach(uriBuilder::query);
                    uriBuilder.queryParam(QUERY_PARAM_NAME, UriUtils.encode(query, UTF_8_ENCODING));
                    if (queryFilter != null) {
                        for(String value : queryFilter)
                            uriBuilder.queryParam(QF_PARAM_NAME, UriUtils.encode(value, UTF_8_ENCODING));
                    }
                    if (onlyIiif) {
                        uriBuilder.queryParam(QF_PARAM_NAME, UriUtils.encode(searchApiIiifQuery, UTF_8_ENCODING));
                    }
                    if (!otherParams.isEmpty()) {
                        otherParams.forEach((k, v) -> uriBuilder.queryParam(k.toLowerCase(), UriUtils.encode(v, UTF_8_ENCODING)));
                    }
                    return uriBuilder.queryParam(CURSOR_PARAM_NAME, UriUtils.encode(cursor, UTF_8_ENCODING))
                            .build();
                })
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(EuropeanaSearchResponse.class)
                .cast(SearchResponse.class);
    }

    private void checkParameters(String query, String cursor) {
        if (StringUtils.isEmpty(query)) {
            throw new IllegalStateException("Mandatory parameter (query) is missing");
        }
        if (StringUtils.isEmpty(cursor)) {
            throw new IllegalStateException("Cursor cannot be empty, it has to be either * or a value from previous request");
        }
    }

    @Override
    public Mono<SearchResponse> search(String query, Map<String, String> requestParams, int rowsPerPage) {
        List<String> qf = null;
        String cursor;
        boolean onlyIiif;

        if (query.isEmpty()) {
            query = QUERY_ALL;
        }

        String qfParam = requestParams.get(QF_PARAM_NAME);
        if (!(qfParam == null || qfParam.isEmpty())) {
            if(qfParam.contains(FACET_SEPARATOR)) {
                qf = new ArrayList<>(Arrays.asList(qfParam.split(FACET_SEPARATOR)));
            } else {
                qf = Collections.singletonList(qfParam);
            }

        }

        String cursorParam = requestParams.get(CURSOR_PARAM_NAME);
        if (cursorParam == null || cursorParam.isEmpty()) {
            cursor = FIRST_CURSOR;
        } else {
            cursor = cursorParam;
        }

        String onlyIiifParam = requestParams.get(ONLY_IIIF_PARAM_NAME);
        if (onlyIiifParam == null || onlyIiifParam.isEmpty()) {
            onlyIiif = true;
        } else {
            onlyIiif = Boolean.parseBoolean(onlyIiifParam);
        }

        Map<String, String> otherParams = new HashMap<>();
        requestParams.forEach((k, v) -> {
            String joinValue = String.join(",", v);
            otherParams.put(k, joinValue);
        });
        otherParams.keySet().removeAll(Arrays.asList(FIXED_API_PARAMS));

        otherParams.put(ROWS_PARAM_NAME, String.valueOf(rowsPerPage));

        return search(query, qf, cursor, onlyIiif, otherParams);
    }
}
