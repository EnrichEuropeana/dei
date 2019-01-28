package pl.psnc.dei.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;

public class RestRequestExecutor {

    protected WebClient webClient;

    public RestRequestExecutor() {
    }

    /**
     * Creates an instance of web client used by the executor.
     * @param webClientBuilder web client builder (by default automatically created by spring)
     */
    protected void configure(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Sets the root URI in the web client used by the executor.
     * @param rootUri root URI used for each request
     */
    protected void setRootUri(String rootUri) {
        URI uri = validateUri(rootUri);
        if (webClient != null) {
            webClient = webClient.mutate()
                    .baseUrl(uri.toString())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
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
