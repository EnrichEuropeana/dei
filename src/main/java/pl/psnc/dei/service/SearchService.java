package pl.psnc.dei.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.response.search.SearchResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;

@Service
public class SearchService
extends RestRequestExecutor {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Value("${api.key}")
    private String apiKey;

    @Value("${search.api.url}")
    private String searchApiUrl;

    @Value("#{'${search.api.predefined}'.split(',')}")
    private List<String> searchApiPredefined;

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
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam("wskey", apiKey);
                    searchApiPredefined.forEach(s -> uriBuilder.query(s));
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
}
