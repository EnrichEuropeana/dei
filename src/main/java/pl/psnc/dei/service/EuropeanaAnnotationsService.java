package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.util.CallToActionBuilder;
import pl.psnc.dei.util.TranscriptionConverter;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EuropeanaAnnotationsService extends RestRequestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(EuropeanaAnnotationsService.class);

    private static final Pattern ANNOTATION_ID_PATTERN = Pattern.compile(".*\\/([0-9]*)");

    private static final String ID = "id";

    private final AccessTokenManager accessTokenManager;

    private final TranscriptionConverter transcriptionConverter;

    private final CallToActionBuilder callToActionBuilder;

    @Value("${europeana.api.annotations.endpoint}")
    private String annotationApiEndpoint;

    @Value("${api.userToken}")
    private String userToken;

    @Autowired
    public EuropeanaAnnotationsService(WebClient.Builder webClientBuilder, AccessTokenManager accessTokenManager,
            TranscriptionConverter transcriptionConverter, CallToActionBuilder callToActionBuilder) {
        this.accessTokenManager = accessTokenManager;
        this.transcriptionConverter = transcriptionConverter;
        this.callToActionBuilder = callToActionBuilder;
        configure(webClientBuilder);
    }

    @PostConstruct
    private void configure() {
        setRootUri(annotationApiEndpoint);
        logger.info("Will use {} url.", annotationApiEndpoint);
        if (userToken == null || userToken.isEmpty()) {
            userToken = accessTokenManager.getAccessToken();
        }
    }


    /**
     * Sends prepared transcription (generated by Transcripton platform) to Europeana using annotationApiEndpoint
     *
     * @param transcription JSON that contains target, body and optionally annotation metadata
     * @return String that contains annotationId generated for given transcription
     */
    public String postTranscription(Transcription transcription) {
        logger.info("Sending transcription to Annotations API. Transcription: " +
                transcription.getTranscriptionContent().toString());
        String annotationResponse = webClient.post()
                .uri(annotationApiEndpoint)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(transcription.getTranscriptionContent().toString()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                        logger.warn("Access token expired. Requesting new one.");
                        return Mono.empty();
                    } else {
                        logger.error("Error {} while posting transcription. Cause: {}", clientResponse.rawStatusCode(),
                                clientResponse.statusCode().getReasonPhrase());
                        return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                                clientResponse.statusCode().getReasonPhrase()));
                    }
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} while posting transcription. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(throwable -> logger.error(throwable.getMessage()))
                .block();
        if (annotationResponse == null || annotationResponse.isEmpty()) {
            userToken = accessTokenManager.getAccessTokenWithRefreshToken();
            return postTranscription(transcription);
        }
        return extractAnnotationId(annotationResponse);
    }

    /**
     * Sends updated transcription (generated by Transcripton platform) to Europeana using annotationApiEndpoint
     *
     * @param transcription
     * @return
     */
    public String updateTranscription(Record record, Transcription transcription) {
        JsonObject convertedTranscription = transcriptionConverter.convert(record,
                transcription.getTranscriptionContent());
        String annotationResponse = webClient.put()
                .uri(b -> b.path((annotationApiEndpoint.endsWith("/") ? "" : "/") + transcription.getAnnotationId())
                        .build())
                .header("Authorization", "Bearer " + userToken)
                .body(BodyInserters.fromObject(convertedTranscription.toString()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                        logger.warn("Access token expired. Requesting new one.");
                        return Mono.empty();
                    } else {
                        logger.error("Error {} while updating transcription. Cause: {}", clientResponse.rawStatusCode(),
                                clientResponse.statusCode().getReasonPhrase());
                        return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                                clientResponse.statusCode().getReasonPhrase()));
                    }
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} while updating transcription. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .block();
        if (annotationResponse == null || annotationResponse.isEmpty()) {
            userToken = accessTokenManager.getAccessTokenWithRefreshToken();
            return updateTranscription(record, transcription);
        }
        return extractAnnotationId(annotationResponse);
    }

    private String extractAnnotationId(String annotationResponse) {
        if (annotationResponse != null) {
            JsonValue value = JSON.parseAny(annotationResponse);
            if (value != null && value.getAsObject().get(ID) != null) {
                Matcher matcher = ANNOTATION_ID_PATTERN.matcher(value.getAsObject().get(ID).getAsString().value());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    public void postCallToAction(Record record) {
        logger.info("Sending call to action link to Annotations API. Record identifier: {}, story identifier: {}",
                record.getIdentifier(), record.getStoryId());
        if (record.getStoryId() == null) {
            throw new IllegalArgumentException("No story id in record " + record.getIdentifier());
        }
        userToken = accessTokenManager.getAccessTokenWithRefreshToken();
        String annotationResponse = webClient.post()
                .uri(annotationApiEndpoint)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(callToActionBuilder.fromRecord(record).toString()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(s -> {
                            if (clientResponse.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                                logger.warn("Send Call to action - bad request. Response: {}", s);
                                return Mono.empty();
                            } else {
                                logger.error("Error {} while posting call to action. Cause: {}",
                                        clientResponse.rawStatusCode(),
                                        s);
                                return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                                        s));
                            }
                        }))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} while posting call to action. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(throwable -> logger.error(throwable.getMessage()))
                .block();
        if (!(annotationResponse == null || annotationResponse.isEmpty())) {
            logger.info("Call to action annotation id: {}", annotationResponse);
        }
    }
}
