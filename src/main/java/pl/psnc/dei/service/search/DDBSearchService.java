package pl.psnc.dei.service.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.response.search.ddb.DDBSearchResponse;
import pl.psnc.dei.schema.search.DDBOffsetPagination;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import static pl.psnc.dei.util.DDBConstants.*;

@Service
public class DDBSearchService extends RestRequestExecutor implements AggregatorSearchService {

	private static final Logger logger = LoggerFactory.getLogger(DDBSearchService.class);

	@Value("${ddb.api.key}")
	private String apiKey;

	@Value("${ddb.search.api.url}")
	private String searchApiUrl;

	@Value("${ddb.api.url}")
	private String ddbApiUri;

	public DDBSearchService(WebClient.Builder webClientBuilder) {
		configure(webClientBuilder);
	}

	@PostConstruct
	private void configure() {
		setRootUri(ddbApiUri);
		logger.info("Will use {} url.", ddbApiUri);
	}

	@Override
	public Mono<SearchResponse> search(String query, Map<String, String> requestParams, int rowsPerPage) {
		SearchResponse result = webClient.get()
				.uri(buildUri(query, requestParams, rowsPerPage))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.bodyToMono(DDBSearchResponse.class)
				.cast(SearchResponse.class)
				.block();

		int offset = getOffsetFromParams(requestParams);
		result.setPagination(new DDBOffsetPagination(rowsPerPage, offset + rowsPerPage));

		return Mono.just(result);
	}

	private int getOffsetFromParams(Map<String, String> requestParams) {
		int offset;
		String offsetParam = requestParams.get(OFFSET_PARAM_NAME);
		if (offsetParam == null || offsetParam.isEmpty()) {
			offset = 0;
		} else {
			offset = Integer.parseInt(offsetParam);
		}
		return offset;
	}

	private Function<UriBuilder, URI> buildUri(String query, Map<String, String> requestParams, int rowsPerPage) {
		return uriBuilder -> {
			uriBuilder.path(searchApiUrl).queryParam(DDB_API_KEY_PARAM_NAME, apiKey);
			if (!query.isEmpty()) {
				uriBuilder.queryParam(QUERY_PARAM_NAME, UriUtils.encode(query, UTF_8_ENCODING));
			}

			fillFacets(uriBuilder);
			uriBuilder.queryParam(ROWS_PARAM_NAME, rowsPerPage);
			uriBuilder.queryParam(OFFSET_PARAM_NAME,  getOffsetFromParams(requestParams));
			return uriBuilder.build();
		};
	}

	private void fillFacets(UriBuilder uriBuilder) {
		for(String facet: FACET_NAMES) {
			uriBuilder.queryParam(FACET_PARAM_NAME, facet);
		}
	}
}
