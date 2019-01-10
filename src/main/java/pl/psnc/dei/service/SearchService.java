package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    @Value("${api.key}")
    private String apiKey;

    public String search(String query, String queryFilter) {
        // consider using europeana client instead of http request
        return String.format("Using api key: %s. Query: %s. Query filter: %s. Implement searching to see real results!", apiKey, query, queryFilter);
    }
}
