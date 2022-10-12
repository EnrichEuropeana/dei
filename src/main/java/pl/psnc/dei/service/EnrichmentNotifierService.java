package pl.psnc.dei.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.ParseRecordsException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.CustomStreamUtils;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class EnrichmentNotifierService {

    public static final String RECORD_PARAM = "record";
    private static final String NATIONAL_AGGREGATOR_NOTIFY_ENDPOINT =
            "/enrichment";
    private static final String CONTENT_PROVIDER_NOTIFY_ENDPOINT =
            "/enrichment";
    private final EuropeanaSearchService europeanaSearchService;
    private final boolean IS_MOCK = true;

    public EnrichmentNotifierService(EuropeanaSearchService europeanaSearchService) {
        this.europeanaSearchService = europeanaSearchService;
    }


    public void notifyPublishers(Record record) {
        JsonObject recordJson = getRecordJson(record);
        String oai = extractOai(recordJson);
        try {
            notifyContentProvider(recordJson, oai);
        } catch (IOException | URISyntaxException e) {
            log.error("Unable to notify content provider. Reason: {}", e.getMessage());
        }
        try {
            notifyNationalAggregator(recordJson, oai);
        } catch (IOException | URISyntaxException e) {
            log.error("Unable to notify national aggregator. Reason: {}", e.getMessage());
        }
    }

    private void notifyContentProvider(JsonObject recordJson, String oai) throws IOException, URISyntaxException {
        List<URL> contentProviderUrls = extractContentProviderUrls(recordJson);
        for (URL providerUrl : contentProviderUrls) {
            sendNotification(providerUrl, CONTENT_PROVIDER_NOTIFY_ENDPOINT, oai);
        }
    }

    private void notifyNationalAggregator(JsonObject recordJson, String oai) throws IOException, URISyntaxException {
        URL nationalAggregatorUrl = extractNationalAggregatorUrl(recordJson);
        sendNotification(nationalAggregatorUrl, NATIONAL_AGGREGATOR_NOTIFY_ENDPOINT, oai);
    }

    private JsonObject getRecordJson(Record record) {
        return europeanaSearchService.retrieveRecordInJson(record.getIdentifier());
    }

    protected List<URL> extractContentProviderUrls(JsonObject recordJson) {
        return recordJson.get("object").getAsObject()
                .get("aggregations").getAsArray()
                .stream()
                .map(jsonValue -> jsonValue.getAsObject()
                        .get("edmObject")
                        .getAsString()
                        .value())
                .map(string -> {
                    try {
                        return new URL(string);
                    } catch (MalformedURLException e) {
                        log.error("Cannot create URL for {}, reason {}", string, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(CustomStreamUtils.distinctByKey(URL::getAuthority))
                .collect(Collectors.toList());
    }

    protected String extractOai(JsonObject recordJson) throws ParseRecordsException {
        return getDcIdentifiersStream(recordJson)
                .filter(s -> s.startsWith("oai:"))
                .findFirst()
                .orElseThrow(() -> new ParseRecordsException("Unable to extract OAI identifier from record json"));
    }

    protected URL extractNationalAggregatorUrl(JsonObject recordJson) throws ParseRecordsException, MalformedURLException {
        String urlString = getDcIdentifiersStream(recordJson)
                .filter(s -> s.startsWith("http"))
                .findFirst()
                .orElseThrow(() -> new ParseRecordsException("Unable to extract national provider URL"));
        return new URL(urlString);
    }

    private Stream<String> getDcIdentifiersStream(JsonObject recordJson) {
        return recordJson.get("object").getAsObject()
                .get("proxies").getAsArray()
                .stream()
                .map(JsonValue::getAsObject)
                .map(jsonObject -> jsonObject.get("dcIdentifier").getAsObject())
                .flatMap(jsonValue -> jsonValue.get("def").getAsArray().stream())
                .map(jsonValue -> jsonValue.getAsString().value());
    }

    protected boolean sendNotification(URL objectUrl, String path, String recordId) throws URISyntaxException, IOException {
        URL url = new URIBuilder().setHost(objectUrl.getHost())
                .setScheme(objectUrl.getProtocol())
                .setPort(objectUrl.getPort())
                .setPath(path)
                .setParameter(RECORD_PARAM, recordId)
                .build().toURL();
        log.info("Calling POST {}", url);
        if (!IS_MOCK) {
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            int responseCode = connection.getResponseCode();
            return HttpStatus.valueOf(responseCode).is2xxSuccessful();
        } else {
            log.info("Notification sending is not implemented yet!");
            return true;
        }
    }
}
