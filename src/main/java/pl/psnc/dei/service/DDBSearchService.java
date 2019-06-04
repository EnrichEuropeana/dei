package pl.psnc.dei.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.response.search.ddb.DDBSearchResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
@Service
public class DDBSearchService extends RestRequestExecutor implements AggregatorSearchService {

	private static final Logger log = LoggerFactory.getLogger(DDBSearchService.class);

	private static final String UTF_8_ENCODING = "UTF-8";
	private final String DDB_API_KEY_NAME = "oauth_consumer_key";
	private final String QUERY = "query";
	private final String ROWS = "rows";
	private final String OFFSET = "offset";
	private final int DEFAULT_NUMBER_OF_ROWS = 10;

	@Value("${ddb.api.key}")
	private String apiKey;

	@Value("${ddb.search.api.url}")
	private String searchApiUrl;

	@Value("${ddb.api.uri}")
	private String ddbApiUri;

	public DDBSearchService(WebClient.Builder webClientBuilder) {
		configure(webClientBuilder);
	}

	@PostConstruct
	private void configure() {
		setRootUri(ddbApiUri);
		log.info("Will use {} url.", ddbApiUri);
	}

	@Override
	public Mono<SearchResponse> search(String query, Map<String, String> requestParams) {
		return null;
	}

	@GetMapping("/test")
	public DDBSearchResponse search() {
		String query = "poland";
		Map<String, String> requestParams = new HashMap<>();


		DDBSearchResponse result = webClient.get()
				.uri(buildUri(query, requestParams))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.bodyToMono(DDBSearchResponse.class)
				.block();
		return result;
	}

	private Function<UriBuilder, URI> buildUri(String query, Map<String, String> requestParams) {
		return uriBuilder -> {
			int page;
			int rows;

			String numberOfRows = requestParams.get("rows");
			if (numberOfRows == null || numberOfRows.isEmpty()) {
				rows = DEFAULT_NUMBER_OF_ROWS;
			} else {
				rows = Integer.parseInt(numberOfRows);
			}

			String pageNumber = requestParams.get("page_number");
			if (pageNumber == null || pageNumber.isEmpty()) {
				page = 0;
			} else {
				page = Integer.parseInt(pageNumber);
			}

			uriBuilder.path(searchApiUrl).queryParam(DDB_API_KEY_NAME, apiKey);
			if (!query.isEmpty()) {
				uriBuilder.queryParam(QUERY, UriUtils.encode(query, UTF_8_ENCODING));
			}

			uriBuilder.queryParam(ROWS, rows);
			uriBuilder.queryParam(OFFSET, page * rows);
			return uriBuilder.build();
		};
	}
}
