package pl.psnc.dei.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecordRetrieverService extends RestRequestExecutor {

    private final Logger log = LoggerFactory.getLogger(RecordRetrieverService.class);
    private final WebClient webClient;
    @Value("${api.key}")
    private String apiKey;
    @Value("${record.api.url}")
    private String recordApiUrl;

    public RecordRetrieverService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(recordApiUrl).build();
    }

    public String retriveRecord(String datasetId, String localId) {
        log.info("Retrieving record {} {}", datasetId, localId);
        Map<String, String> m = new HashMap<>();
        // TODO how record id is looks like
        m.putIfAbsent("dataset", datasetId);
        m.putIfAbsent("localId", localId);
        m.putIfAbsent("format", "rdf");
        String record = webClient.get()
                .uri(recordApiUrl + "/{dataset}/{localId}.{format}?wskey={apiKey}" + apiKey, m)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(String.class)
                .block();

        return record;
        //TODO change returning type after jena research
    }
}
