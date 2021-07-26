package pl.psnc.dei.service.search;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.response.search.ddb.DDBSearchResponse;
import pl.psnc.dei.schema.search.DDBOffsetPagination;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
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

	@Value("${ddb.api.itemsEndpoint}")
	private String ddbApiItemsEndpoint;

	@Value("${ddb.api.key}")
	private String oauth_key;

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

	public JsonObject retrieveRecordAndConvertToJsonLd(String recordId) {
		logger.info("Retrieving record from ddb {}", recordId);

		String xmlRecord = webClient.get()
				.uri(b -> b.path(ddbApiItemsEndpoint).queryParam("oauth_consumer_key", oauth_key).build(recordId))
				.accept(MediaType.APPLICATION_XML)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.bodyToMono(String.class)
				.block();

		return convertToJsonLd(xmlRecord);
	}

	@Override
	public Set<String> getAllDatasetRecords(String datasetId) {
		throw new NotImplementedException("Not implemented");
	}

	private JsonObject convertToJsonLd(String record) {
		final Model model = ModelFactory.createDefaultModel();
		String rdfString = getRdfPartFromXML(record);
		model.read(new ByteArrayInputStream(rdfString.getBytes(StandardCharsets.UTF_8)), "RDF/XML");
		final StringWriter writer = new StringWriter();
		model.write(writer, "JSON-LD");
		return JSON.parse(writer.toString());
	}

	private String getRdfPartFromXML(String record) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Node rdf = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(record)));
			document.getDocumentElement().normalize();
			rdf = document.getElementsByTagName("rdf:RDF").item(0);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.error("xml conversion error", e);
		}
		return nodeToString(rdf);
	}

	private String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			logger.error("xml conversion error", te);
		}
		return sw.toString();
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
			handleRequestParams(uriBuilder, requestParams);
			uriBuilder.queryParam(ROWS_PARAM_NAME, rowsPerPage);
			uriBuilder.queryParam(OFFSET_PARAM_NAME,  getOffsetFromParams(requestParams));
			return uriBuilder.build();
		};
	}

	private void fillFacets(UriBuilder uriBuilder) {
		for(String facet: FACET_NAMES) {
			uriBuilder.queryParam(FACET_PARAM_NAME, UriUtils.encode(facet, UTF_8_ENCODING));
		}
	}

	private void handleRequestParams(UriBuilder uriBuilder, Map<String, String> requestParams) {
		requestParams.entrySet().stream()
				.filter(e -> !e.getKey().equals(ROWS_PARAM_NAME) && !e.getKey().equals(OFFSET_PARAM_NAME))
				.forEach(e -> Arrays.stream(e.getValue().split("---"))
						.forEach(v -> uriBuilder.queryParam(e.getKey(), UriUtils.encode(v, UTF_8_ENCODING))));
	}
}
