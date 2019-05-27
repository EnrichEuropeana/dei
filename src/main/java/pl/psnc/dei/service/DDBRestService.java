package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

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
import java.nio.charset.StandardCharsets;

@Service
public class DDBRestService extends RestRequestExecutor {

	private final Logger logger = LoggerFactory.getLogger(DDBRestService.class);

	@Value("${ddb.api.items}")
	private String ddbApiItems;

	@Value("${ddb.api.key}")
	private String oauth_key;

	public DDBRestService(WebClient.Builder webClientBuilder) {
		configure(webClientBuilder);
	}

	public JsonObject retrieveRecordFromEuropeanaAndConvertToJsonLd(String recordId) {
		recordId = "OAXO2AGT7YH35YYHN3YKBXJMEI77W3FF";
		logger.info("Retrieving record from ddb {}", recordId);

		final String url = ddbApiItems + "/" + recordId + "/edm?oauth_consumer_key=" + oauth_key;

		String xmlRecord = webClient.get()
				.uri(url)
				.header("Accept", "application/xml")
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.bodyToMono(String.class)
				.block();

		return convertToJsonLd(xmlRecord);
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
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("xml conversion error", e);
		}
		Document document = null;
		try {
			document = builder.parse(new InputSource(new StringReader(record)));
		} catch (SAXException | IOException e) {
			logger.error("xml conversion error", e);
		}
		document.getDocumentElement().normalize();
		Node rdf = document.getElementsByTagName("rdf:RDF").item(0);
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
}
