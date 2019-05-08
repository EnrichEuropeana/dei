package pl.psnc.dei.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.StringWriter;

@Service
public class EuropeanaRestService extends RestRequestExecutor {

	private final Logger logger = LoggerFactory.getLogger(EuropeanaRestService.class);

	@Value("${europeana.api.url}")
	private String europeanaApiUrl;

	@Value("${europeana.api.annotations.endpoint}")
	private String annotationApiEndpoint;

	@Value("${europeana.api.record.endpoint}")
	private String recordApiEndpoint;

	@Value("${api.key}")
	private String apiKey;

	@Value("${api.userToken}")
	private String userToken;

    public EuropeanaRestService(WebClient.Builder webClientBuilder) {
    	configure(webClientBuilder);
    }

    @PostConstruct
    private void init() {
    	if(StringUtils.isNotBlank(europeanaApiUrl))
			setRootUri(europeanaApiUrl);
    }

    /**
     * @param transcription JSON that contains target, body and optionally annotation metadata
     * @return String that contains annotationId generated for given transcription
     */
    public String postTranscription(Transcription transcription) {
        String annotationId = webClient.post()
                .uri(b -> b.path(annotationApiEndpoint).queryParam("wskey", apiKey).queryParam("userToken", userToken).build())
//                TODO when parser will be ready change transcription to some kind of json object?
                .body(BodyInserters.fromObject(transcription))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(String.class)
                .block();

        return annotationId;
    }

    public String updateTranscription(Transcription transcription) {
        String annotationId = webClient.put()
                .uri(b -> b.path(annotationApiEndpoint).queryParam("wskey", apiKey).queryParam("userToken", userToken).build())
//                TODO when parser will be ready change transcription to some kind of json object?
                .body(BodyInserters.fromObject(transcription))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(String.class)
                .block();

		return annotationId;
	}

	public JsonObject retriveRecordFromEuropeanaAndConvertToJsonLd(String recordId) {
		logger.info("Retrieving record from europeana {}", recordId);
		final String url = europeanaApiUrl + recordApiEndpoint + recordId + ".rdf?wskey=" + apiKey;
		final Model model = ModelFactory.createDefaultModel();
		model.read(url);
		final StringWriter writer = new StringWriter();
		model.write(writer, "JSON-LD");
		return JSON.parse(writer.toString());
	}
}
