package pl.psnc.dei.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import pl.psnc.dei.exception.DEIHttpException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

public class RestRequestExecutor {

    protected WebClient webClient;

    private final ExchangeFilterFunction errorResponseFilter = ExchangeFilterFunction
            .ofResponseProcessor(this::exchangeFilterResponseProcessor);

    /**
     * Creates an instance of web client used by the executor.
     * @param webClientBuilder web client builder (by default automatically created by spring)
     */
    protected void configure(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    protected Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response) {
        HttpStatus status = response.statusCode();
        if (HttpStatus.UNAUTHORIZED.equals(status)) {
            return Mono.empty();
        }
        if (status.isError()) {
            return response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new DEIHttpException(status.value(),
                            body)));
        }
        return Mono.just(response);
    }

    /**
     * Sets the root URI in the web client used by the executor.
     * @param rootUri root URI used for each request
     */
    protected void setRootUri(String rootUri) {
        URI uri = validateUri(rootUri);
        if (webClient != null) {
            DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(uri.toString());
            uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
            webClient = webClient.mutate()
                    .baseUrl(uri.toString())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                    .filter(errorResponseFilter)
                    .uriBuilderFactory(uriBuilderFactory)
                    .build();
        }
    }

    private URI validateUri(String rootUri) {
        if (rootUri != null && !rootUri.isEmpty()) {
            try {
                return new URI(rootUri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(rootUri + " is not correct URI value");
            }
        }
        throw new IllegalArgumentException("Null or empty string is not correct URI value");
    }
}
