package pl.psnc.dei.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class EuropeanaRestService extends RestRequestExecutor {

	private final Logger logger = LoggerFactory.getLogger(EuropeanaRestService.class);

	@Value("${europeana.api.url}")
	private String europeanaApiUrl;

	@Value("${europeana.api.record.endpoint}")
	private String recordApiEndpoint;

	@Value("${europeana.api.annotations.endpoint}")
	private static String annotationApiEndpoint;

	@Value("${api.key}")
	private String apiKey;

	@Value("${api.userToken}")
	private String userToken;

	public EuropeanaRestService() {
	}

	@PostConstruct
	private void init() {
		setRootUri(europeanaApiUrl);
	}

	public String retriveRecord(String datasetId, String localId) {
		logger.info("Retrieving record {} {}", datasetId, localId);
		Map<String, String> m = new HashMap<>();
		// TODO how record id is looks like
		m.putIfAbsent("dataset", datasetId);
		m.putIfAbsent("localId", localId);
		m.putIfAbsent("format", "rdf");
		String record = webClient.get()
				.uri( recordApiEndpoint+ "/{dataset}/{localId}.{format}?wskey=" + apiKey, m)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.bodyToMono(String.class)
				.block();

		return record;
		//TODO change returning type after jena research
	}

	/**
	 *
	 * @param transcription JSON that contains target, body and optionally annotation metadata
	 * @return String that contains annotationId generated for given transcription
	 */
//	TODO change String transcription to Transcription transcription after merge
	public String postTranscription(String transcription) {
		String annotationId = webClient.post()
				.uri(b -> b.path(annotationApiEndpoint).queryParam("wskey", apiKey).queryParam("userToken", userToken).build())
				.body(BodyInserters.fromObject(transcription))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.bodyToMono(String.class)
				.block();

		return annotationId;
	}

}