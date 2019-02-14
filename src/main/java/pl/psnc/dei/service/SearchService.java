package pl.psnc.dei.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.response.search.SearchResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class SearchService
extends RestRequestExecutor {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Value("${api.key}")
    private String apiKey;

    @Value("${search.api.url}")
    private String searchApiUrl;

    @Value("#{'${search.api.predefined.parameters}'.split(',')}")
    private List<String> searchApiPredefinedParameters;

    @Value("${search.api.iiif.query}")
    private String searchApiIiifQuery;

    public SearchService(WebClient.Builder webClientBuilder) {
        configure(webClientBuilder);
    }

    @PostConstruct
    private void configure() {
        setRootUri(searchApiUrl);
        log.info("Will use {} url.", searchApiUrl);
    }

    public Mono<SearchResponse> search(String query, String queryFilter, String cursor) {
        checkParameters(query, cursor);
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam("wskey", apiKey);
                    searchApiPredefinedParameters.forEach(uriBuilder::query);
                    return uriBuilder.queryParam("query", query)
                        .queryParam("qf", queryFilter)
                        .queryParam("qf", searchApiIiifQuery)
                        .queryParam("cursor", cursor)
                        .build();
                })
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(SearchResponse.class);
    }

    private void checkParameters(String query, String cursor) {
        if (StringUtils.isEmpty(query)) {
            throw new IllegalStateException("Mandatory parameter (query) is missing");
        }
        if (StringUtils.isEmpty(cursor)) {
            throw new IllegalStateException("Cursor cannot be empty, it has to be either * or a value from previous request");
        }
    }
}
