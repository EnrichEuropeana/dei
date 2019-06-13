package pl.psnc.dei.util.ddb;


import net.minidev.json.JSONObject;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class DDBFormatResolver extends RestRequestExecutor {

	private static final Logger log = LoggerFactory.getLogger(DDBFormatResolver.class);
	public static final String MIMETYPE = "@mimetype";

	private final String DDB_API_KEY_NAME = "oauth_consumer_key";

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
		JSONObject result = webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(formatApiUri);
					uriBuilder.queryParam(DDB_API_KEY_NAME, apiKey);
					return uriBuilder.build(recordId);
				})
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()))				)
				.bodyToMono(JSONObject.class)
				.onErrorReturn(new JSONObject())
				.block();
		if (result == null || result.isEmpty()) {
			return null;
		}

		Object binary = result.get("binary");
		if (binary instanceof List) {
			List<HashMap<String, String>> binaries = (ArrayList<HashMap<String, String>>) binary;
			boolean allMatchSameMimeType = binaries.stream().allMatch(b -> b.containsKey(MIMETYPE) && b.get(MIMETYPE).equals(binaries.get(0).get(MIMETYPE)));
			if (allMatchSameMimeType) {
				return binaries.get(0).get(MIMETYPE);
			}
			return null;
		}

		HashMap<String, String> fields = (HashMap<String, String>) result.get("binary");
		if (fields == null || fields.isEmpty()) {
			return null;
		}
		return fields.get(MIMETYPE);
	}

}
