package pl.psnc.dei.service.search;

import com.google.common.collect.ImmutableMap;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import pl.psnc.dei.exception.AggregatorException;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.exception.EuropeanaAggregatorException;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.response.search.europeana.EuropeanaFacet;
import pl.psnc.dei.response.search.europeana.EuropeanaFacetField;
import pl.psnc.dei.response.search.europeana.EuropeanaSearchResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static pl.psnc.dei.ui.components.facets.EuropeanaFacetComponent.FACET_SEPARATOR;
import static pl.psnc.dei.ui.pages.SearchPage.ONLY_IIIF_PARAM_NAME;
import static pl.psnc.dei.util.EuropeanaConstants.*;

@Service
public class EuropeanaSearchService extends RestRequestExecutor implements AggregatorSearchService {

    private static final Logger logger = LoggerFactory.getLogger(EuropeanaSearchService.class);

    @Value("${europeana.api.key}")
    private String apiKey;

    @Value("${europeana.search.api.url}")
    private String searchApiUrl;

    @Value("${europeana.api.record.endpoint}")
    private String recordApiEndpoint;

    @Value("#{'${europeana.search.api.predefined.parameters}'.split(',')}")
    private List<String> searchApiPredefinedParameters;

    @Value("${europeana.search.api.iiif.query}")
    private String searchApiIiifQuery;

    private static final Map<String, String> ALL_DATASET_RECORDS_QUERY_PARAMS = ImmutableMap.of(
            "facet", "europeana_id",
            "profile", "facets",
            "f.europeana_id.facet.limit", "500000",
            "rows", "0"
    );

    public EuropeanaSearchService(WebClient.Builder webClientBuilder) {
        configure(webClientBuilder);
    }

    @PostConstruct
    private void configure() {
        setRootUri(searchApiUrl);
        logger.info("Will use {} url.", searchApiUrl);
    }

    /**
     * Execute search request. Note that cursor parameter must be URL encoded.
	 *
     * @param query query string
     * @param queryFilter query filter
     * @param cursor cursor for next page of values
     * @param onlyIiif true to query only objects available via IIIF, false otherwise
     * @param otherParams other request parameters e.g. media, reusability
     * @return response from search API associated with web client
     */
    public Mono<SearchResponse> search(String query, List<String> queryFilter, String cursor, boolean onlyIiif, Map<String, String> otherParams) {
        checkParameters(query, cursor);
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam(API_KEY_PARAM_NAME, apiKey);
                    searchApiPredefinedParameters.forEach(uriBuilder::query);
                    uriBuilder.queryParam(QUERY_PARAM_NAME, UriUtils.encode(query, UTF_8_ENCODING));
                    if (queryFilter != null) {
                        for(String value : queryFilter)
                            uriBuilder.queryParam(QF_PARAM_NAME, UriUtils.encode(value, UTF_8_ENCODING));
                    }
                    if (onlyIiif) {
                        uriBuilder.queryParam(QF_PARAM_NAME, UriUtils.encode(searchApiIiifQuery, UTF_8_ENCODING));
                    }
                    if (!otherParams.isEmpty()) {
                        otherParams.forEach((k, v) -> uriBuilder.queryParam(k.toLowerCase(), UriUtils.encode(v, UTF_8_ENCODING)));
                    }
                    return uriBuilder.queryParam(CURSOR_PARAM_NAME, UriUtils.encode(cursor, UTF_8_ENCODING))
                            .build();
                })
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(EuropeanaSearchResponse.class)
                .cast(SearchResponse.class);
    }

    /**
     * Retrieves Europeana record from recordsApiEndpoint and transfers is to the JSON_LD format.
     *
     * @param recordId record identifier that will be used for retrieval
     * @return Retrieved record in JSON_LD format
     */
    public JsonObject retrieveRecordAndConvertToJsonLd(String recordId) {
        logger.info("Retrieving record from europeana {}", recordId);
        String record = webClient.get()
                .uri(recordApiEndpoint + "/" + recordId + ".json-ld?wskey=" + apiKey)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.error("Error {} while retrieving record. Cause: {}", clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} while retrieving record. Cause: {}", clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof DEIHttpException) {
                        String message = cause.toString();
                        throw new EuropeanaAggregatorException(message, cause);
                    } else {
                        throw new EuropeanaAggregatorException(cause.getMessage(), cause);
                    }
                })
                .block();
        return JSON.parse(record);
    }

    /**
     * Retrieves Europeana record from recordsApiEndpoint and transfers is to the JSON format.
     *
     * @param recordId record identifier that will be used for retrieval
     * @return Retrieved record in JSON format
     */
    public JsonObject retrieveRecordInJson(String recordId) throws AggregatorException {
        logger.info("Retrieving record from europeana {}", recordId);
        String record = webClient.get()
                .uri(recordApiEndpoint + "/" + recordId + ".json?wskey=" + apiKey)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.error("Error {} while retrieving record. Cause: {}", clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} while retrieving record. Cause: {}", clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof DEIHttpException) {
                        String message = cause.toString();
                        throw new EuropeanaAggregatorException(message, cause);
                    } else {
                        throw new EuropeanaAggregatorException(cause.getMessage(), cause);
                    }
                })
                .block();
        return JSON.parse(record);
    }

    /**
     * Check if all arguments are not empty
     */
    private void checkParameters(String query, String cursor) {
        if (StringUtils.isEmpty(query)) {
            throw new IllegalStateException("Mandatory parameter (query) is missing");
        }
        if (StringUtils.isEmpty(cursor)) {
            throw new IllegalStateException("Cursor cannot be empty, it has to be either * or a value from previous request");
        }
    }

    @Override
    public Mono<SearchResponse> search(String query, Map<String, String> requestParams, int rowsPerPage) {
        List<String> qf = null;
        String cursor;
        boolean onlyIiif;

        if (query.isEmpty()) {
            query = QUERY_ALL;
            // default is *
        }

        String qfParam = requestParams.get(QF_PARAM_NAME);
        if (!(qfParam == null || qfParam.isEmpty())) {
            if(qfParam.contains(FACET_SEPARATOR)) {
                qf = new ArrayList<>(Arrays.asList(qfParam.split(FACET_SEPARATOR)));
            } else {
                qf = Collections.singletonList(qfParam);
            }

        }

        String cursorParam = requestParams.get(CURSOR_PARAM_NAME);
        if (cursorParam == null || cursorParam.isEmpty()) {
            cursor = FIRST_CURSOR;
        } else {
            cursor = cursorParam;
        }

        String onlyIiifParam = requestParams.get(ONLY_IIIF_PARAM_NAME);
        if (onlyIiifParam == null || onlyIiifParam.isEmpty()) {
            onlyIiif = true;
        } else {
            onlyIiif = Boolean.parseBoolean(onlyIiifParam);
        }

        Map<String, String> otherParams = new HashMap<>();
        requestParams.forEach((k, v) -> {
            String joinValue = String.join("---", v);
            otherParams.put(k, joinValue);
        });
        otherParams.keySet().removeAll(Arrays.asList(FIXED_API_PARAMS));

        otherParams.put(ROWS_PARAM_NAME, String.valueOf(rowsPerPage));

        return search(query, qf, cursor, onlyIiif, otherParams);
    }

    @Override
    public Set<String> getAllDatasetRecords(String datasetId) {
        EuropeanaSearchResponse searchResponse = searchForAllDatasetRecords(datasetId).block();
        EuropeanaFacet idFacet = searchResponse.getFacets().stream()
                .filter(facet -> "europeana_id".equals(facet.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error fetching records for dataset " + datasetId));
        return idFacet.getFields().stream()
                .map(EuropeanaFacetField::getLabel)
                .collect(Collectors.toSet());
    }

    private Mono<EuropeanaSearchResponse> searchForAllDatasetRecords(String datasetId) {
        return webClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam(API_KEY_PARAM_NAME, apiKey);
                    uriBuilder.queryParam(QUERY_PARAM_NAME, UriUtils.encode(String.format("edm_datasetName:%s_*", datasetId), "UTF-8"));
                    ALL_DATASET_RECORDS_QUERY_PARAMS.forEach((k, v) -> uriBuilder.queryParam(k.toLowerCase(), UriUtils.encode(v, "UTF-8")));
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(EuropeanaSearchResponse.class);
    }
}
