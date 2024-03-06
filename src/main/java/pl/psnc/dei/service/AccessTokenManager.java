package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class AccessTokenManager extends RestRequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AccessTokenManager.class);

    @Value("${api.clientId}")
    private String clientId;

    @Value("${api.clientSecret}")
    private String clientSecret;

    @Value("${api.username}")
    private String username;

    @Value("${api.password}")
    private String password;

    @Value("${api.tokenEndpoint}")
    private String tokenEndpoint;

    private String refreshToken;

    private String accessToken;

    public AccessTokenManager(WebClient.Builder webClientBuilder) {
        configure(webClientBuilder);
    }

    @PostConstruct
    private void configure() {
        setRootUri(tokenEndpoint);
        logger.info("Will use {} url.", tokenEndpoint);
    }

    public String getAccessToken() {
        logger.info("Requesting access token");

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("username", username);
        formParams.add("password", password);
        formParams.add("client_id", clientId);
        formParams.add("client_secret", clientSecret);
        formParams.add("grant_type", "password");
        formParams.add("scope", "annotations");

        String authResponse = webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formParams))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.error("Error {} while retrieving access token. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} retrieving access token. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(throwable -> logger.error(throwable.getMessage()))
                .block();
        refreshToken = extractToken(authResponse, TokenType.REFRESH_TOKEN);
        accessToken = extractToken(authResponse, TokenType.ACCESS_TOKEN);
        return accessToken;
    }

    public String getAccessTokenWithRefreshToken() {
        logger.info("Requesting access token using refresh token");

        if (refreshToken == null || refreshToken.isBlank()) {
            return getAccessToken();
        }

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("client_id", clientId);
        formParams.add("grant_type", "refresh_token");
        formParams.add("refresh_token", refreshToken);

        String authResponse = webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .body(BodyInserters.fromFormData(formParams))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.warn("Error {} while retrieving access token. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.empty();
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} retrieving access token using refresh token. Cause: {}",
                            clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(throwable -> logger.error(throwable.getMessage()))
                .block();
        if (authResponse == null || authResponse.isEmpty()) {
            return getAccessToken();
        }
        accessToken = extractToken(authResponse, TokenType.ACCESS_TOKEN);
        return accessToken;
    }

    private String extractToken(String authResponse, TokenType tokenType) {
        if (authResponse != null) {
            JsonValue value = JSON.parseAny(authResponse);
            if (value != null && value.getAsObject().get(tokenType.getValue()) != null) {
                return value.getAsObject().get(tokenType.getValue()).getAsString().value();
            }
        }
        return null;
    }
}
