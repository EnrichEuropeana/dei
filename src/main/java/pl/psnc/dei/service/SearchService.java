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
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class SearchService
extends RestRequestExecutor {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Value("${api.key}")
    private String apiKey;

    @Value("${search.api.url}")
    private String searchApiUrl;

    @Value("${search.api.profile}")
    private String searchApiProfile;

    private String requestUrlPrefix;

    public SearchService(WebClient.Builder webClientBuilder) {
        configure(webClientBuilder);
    }

    @PostConstruct
    private void configure() {
        setRootUri(searchApiUrl);
        log.info("Will use {} url.", searchApiUrl);
        prepareUrlPrefix();
    }

    private void prepareUrlPrefix() {
        requestUrlPrefix = "?wskey=" + apiKey +
                "&profile=" + searchApiProfile + ",facets";
    }

    public Mono<String> search(String query, String queryFilter) {
        String requestUrl = createRequestURL(query, queryFilter);

        return webClient.get()
                .uri(requestUrl, query, queryFilter)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(String.class);
    }

    private String createRequestURL(String query, String queryFilter) {
        StringBuilder requestUrl = new StringBuilder();

        requestUrl.append(requestUrlPrefix);

        if (!StringUtils.isEmpty(query)) {
            if (!StringUtils.isEmpty(queryFilter)) {
                requestUrl.append("&query={query}&qf={queryFilter}");
            } else {
                requestUrl.append("&query={query}");
            }
        } else {
            throw new IllegalStateException("Mandatory parameter (query) is missing");
        }
        return requestUrl.toString();
    }
}
