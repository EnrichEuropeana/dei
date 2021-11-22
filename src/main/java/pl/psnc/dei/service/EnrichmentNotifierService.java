package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.ParseRecordsException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EnrichmentNotifierService {

    private static final String NATIONAL_AGGREGATOR_NOTIFY_ENDPOINT =
            "/enriched/"

    private final EuropeanaSearchService europeanaSearchService;

    public EnrichmentNotifierService(EuropeanaSearchService europeanaSearchService) {
        this.europeanaSearchService = europeanaSearchService;
    }


    public void notifyPublishers(Record record) {
        JsonObject recordJson = getRecordJson(record);
        String oai = extractOai(recordJson);
        notifyContentProvider(recordJson, oai);
        notifyNationalAggregator(recordJson, oai);
    }

    private void notifyContentProvider(JsonObject recordJson, String oai) {

    }

    private void notifyNationalAggregator(JsonObject recordJson, String oai) {

    }

    private JsonObject getRecordJson(Record record) {
        return europeanaSearchService.retrieveRecordInJson(record.getIdentifier());
    }

    private URL extractContentProviderUrl(String recordRaw) {

    }

    private String extractOai(JsonObject recordJson) throws ParseRecordsException {
        return getDcIdentifiersStream(recordJson)
                .filter(s -> s.startsWith("oai:"))
                .findFirst()
                .orElseThrow(() -> new ParseRecordsException("Unable to extract OAI identifier from record json"));
    }

    private URL extractNationalProviderUrl(JsonObject recordJson) throws ParseRecordsException, MalformedURLException {
        String urlString = getDcIdentifiersStream(recordJson)
                .filter(s -> s.startsWith("http"))
                .findFirst()
                .orElseThrow(() -> new ParseRecordsException("Unable to extract national provider URL"));
        return trimUrl(new URL(urlString));
    }

    private Stream<String> getDcIdentifiersStream(JsonObject recordJson) {
        return recordJson.get("object").getAsObject()
                .get("proxies").getAsArray()
                .stream()
                .map(JsonValue::getAsObject)
                .map(jsonObject -> jsonObject.get("dcIdentifier").getAsObject())
                .flatMap(jsonValue -> jsonValue.get("def").getAsArray().stream())
                .map(JsonValue::getAsString)
                .map(JsonValue::toString);
    }

    private URL trimUrl(URL url) throws MalformedURLException {
        return new URL(String.format("%s://%s", url.getProtocol(), url.getAuthority()));
    }
}
