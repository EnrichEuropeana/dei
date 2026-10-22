package pl.psnc.dei.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.iiif.InvalidIIIFManifestException;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
public class IIIFManifestValidator extends RestRequestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(IIIFManifestValidator.class);

    private static final String IIIF_INVALID_MANIFEST_MESSAGE = "IIIF manifest is invalid. Warnings: %s, Error: %s";

    private static final String IIIF_INVALID_MANIFEST_MESSAGE_NO_ERROR = "IIIF manifest has warnings. Warnings: %s";

    @Value("${iiif.manifest.validation.service.url}")
    private String validationServiceURL;

    public IIIFManifestValidator(WebClient.Builder webClientBuilder) {
        configure(webClientBuilder);
    }

    @PostConstruct
    private void configure() {
        setRootUri(validationServiceURL);
        logger.info("IIIF manifest validation will use {} url.", validationServiceURL);
    }

    public void validateIIIFManifest(String manifestURL, String version) throws InvalidIIIFManifestException {
        Optional<ValidationResponse> response = Optional.ofNullable(sendValidationRequest(manifestURL, version));
        if (response.isPresent()) {
            ValidationResponse validationResponse = response.get();
            if (validationResponse.getOkay() == 0) {
                throw new InvalidIIIFManifestException(String.format(IIIF_INVALID_MANIFEST_MESSAGE, validationResponse.getWarnings(), validationResponse.getError()));
                // TODO maybe it would be useful to block sending call to action (which is the final result of the validation) when there are warnings
//            } else if (!validationResponse.getWarnings().isEmpty()) {
//                throw new InvalidIIIFManifestException(String.format(IIIF_INVALID_MANIFEST_MESSAGE_NO_ERROR, validationResponse.getWarnings()));
            }
        } else {
            throw new InvalidIIIFManifestException("Could not validate IIIF manifest file");
        }
    }

    private ValidationResponse sendValidationRequest(String manifestURL, String version) {
        logger.info("Validation of manifest {} in version {}", manifestURL, version);

        return webClient.get()
                .uri(b -> b.queryParam("version", version).queryParam("url", manifestURL).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(ValidationResponse.class)
                .block();
    }
}
