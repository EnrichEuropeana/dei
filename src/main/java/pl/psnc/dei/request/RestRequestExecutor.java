package pl.psnc.dei.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class RestRequestExecutor {

    protected WebClient webClient;

    public RestRequestExecutor() {
    }

    /**
     * Creates an instance of web client used by the executor.
     * @param webClientBuilder web client builder (by default automatically created by spring)
     */
    public void configure(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Sets the root URI in the web client used by the executor.
     * @param rootUri root URI used for each request
     */
    protected void setRootUri(String rootUri) {
        if (rootUri != null && webClient != null) {
            webClient = webClient.mutate()
                    .baseUrl(rootUri)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
    }
}
