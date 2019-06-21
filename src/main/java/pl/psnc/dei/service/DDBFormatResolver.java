package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class DDBFormatResolver extends RestRequestExecutor {

	private static final Logger log = LoggerFactory.getLogger(DDBFormatResolver.class);
	private static final String KEY_MIME_TYPE = "@mimetype";

	private static final String DDB_API_KEY_NAME = "oauth_consumer_key";

	@Value("${ddb.format.api.url}")
	private String formatApiUri;

	@Value("${ddb.api.url}")
	private String ddbApiUri;

	@Value("${ddb.api.key}")
	private String apiKey;

	public DDBFormatResolver(WebClient.Builder webClientBuilder) {
		configure(webClientBuilder);
	}

	@PostConstruct
	private void configure() {
		setRootUri(ddbApiUri);
		log.info("Will use {} url.", ddbApiUri);
	}

	public synchronized String getRecordFormat(String recordId) {
		JsonObject json = getRecordBinariesObject(recordId);
		if (json == null) {
			return null;
		}

		Object binary = json.get("binary");
		if (binary instanceof JsonArray) {
			JsonArray binaries = (JsonArray) binary;
			boolean allMatchSameMimeType = binaries.stream()
					.map(JsonValue::getAsObject)
					.allMatch(b -> b.hasKey(KEY_MIME_TYPE)
							&& b.get(KEY_MIME_TYPE).getAsString().value().equals(binaries.get(0).getAsObject().get(KEY_MIME_TYPE).getAsString().value()));
			if (allMatchSameMimeType) {
				return binaries.get(0).getAsObject().get(KEY_MIME_TYPE).getAsString().value();
			}
			return null;
		}

		JsonObject singleBinary = (JsonObject) binary;
		if (singleBinary == null || singleBinary.isEmpty()) {
			return null;
		}
		return singleBinary.get(KEY_MIME_TYPE).getAsString().value();
	}

	public synchronized JsonObject getRecordBinariesObject(String recordId) {
		String result = webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(formatApiUri);
					uriBuilder.queryParam(DDB_API_KEY_NAME, apiKey);
					return uriBuilder.build(recordId);
				})
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.bodyToMono(String.class)
				.onErrorReturn("")
				.block();
		if (result == null || result.isEmpty() || result.equals("null")) {
			return null;
		}

		return JSON.parse(result);
	}
}
