package pl.psnc.dei.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.ParseRecordsException;
import pl.psnc.dei.model.Record;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

@Service
@Slf4j
public class EnrichmentNotifierService {

    private static final Logger logger = LoggerFactory.getLogger(EnrichmentNotifierService.class);

    @Value("${dri.api.url}")
    private String driApiUrl;

    @Value("${dri.api.username}")
    private String driApiUsername;

    @Value("${dri.api.token:none}")
    private String driApiToken;

    private static final String RECORD_PARAM = "recordId";
    private static final String USER_PARAM = "user_email";
    private static final String TOKEN_PARAM = "user_token";

    private static final String NATIONAL_AGGREGATOR_NOTIFY_ENDPOINT =
            "/enrichments";

    public void notifyPublishers(Record record) {
        try {
            notifyNationalAggregator(record.getIdentifier());
        } catch (IOException | URISyntaxException | ParseRecordsException e) {
            log.error("Unable to notify national aggregator. Reason: {}", e.getMessage());
        }
    }

    private void notifyNationalAggregator(String recordId) throws IOException, URISyntaxException {
        if (!sendNotification(NATIONAL_AGGREGATOR_NOTIFY_ENDPOINT, recordId)) {
            logger.warn("Failed notifying national aggregator about enrichments for record {}", recordId);
        }
    }

    protected boolean sendNotification(String path, String recordId) throws URISyntaxException, IOException {
        URL apiURL = new URL(driApiUrl);
        URL url = new URIBuilder().setHost(apiURL.getHost())
                .setScheme(apiURL.getProtocol())
                .setPort(apiURL.getPort())
                .setPath(path)
                .setParameter(RECORD_PARAM, recordId)
                .setParameter(USER_PARAM, driApiUsername)
                .setParameter(TOKEN_PARAM, driApiToken)
                .build().toURL();
        log.info("Calling POST {}", url);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection connection = (HttpURLConnection) urlConnection;
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        addRequestBody(connection);
        connection.setDoOutput(true);
        int responseCode = connection.getResponseCode();
        return HttpStatus.valueOf(responseCode).is2xxSuccessful();
    }

    private void addRequestBody(HttpURLConnection connection) throws IOException {
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = "{}".getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }
}
