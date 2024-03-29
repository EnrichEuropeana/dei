package pl.psnc.dei.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class GeneralRestRequestService extends RestRequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(GeneralRestRequestService.class);

    public GeneralRestRequestService(WebClient.Builder webClientBuilder) {
        configure(webClientBuilder);
    }

    public Optional<String> downloadFrom(String requestURL) {
        logger.info("Downloading {}", requestURL);
        String response = webClient.get()
                .uri(requestURL)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.error("Error {} while downloading. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} while downloading. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .onErrorResume(throwable -> Mono.empty())
                .block();
        return Optional.ofNullable(response);
    }
}
