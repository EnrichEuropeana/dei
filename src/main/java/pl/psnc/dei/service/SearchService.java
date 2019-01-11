package pl.psnc.dei.service;

import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.model.EuropeanaApi2Results;
import eu.europeana.api.client.search.query.Api2Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Value("${api.key}")
    private String apiKey;

    @Value("${search.api.url}")
    private String searchApiUrl;

    @Value("${search.api.profile}")
    private String searchApiProfile;

    public String search(String query, String queryFilter) {
        EuropeanaApi2Client searchClient = new EuropeanaApi2Client(searchApiUrl, apiKey);
        Api2Query api2query = buildQuery(query, queryFilter);
        try {
            long start = System.currentTimeMillis();
            EuropeanaApi2Results response = (EuropeanaApi2Results) searchClient.search(api2query);
            log.info("Query time: " + String.valueOf(System.currentTimeMillis() - start) + " ms.");
            if (response != null) {
                return response.toJSON();
            }
        } catch (IOException e) {
            log.error("Could not execute search API request.", e);
        }
        return "Error occurred during search...";
    }

    private Api2Query buildQuery(String query, String queryFilter) {
        Api2Query api2Query = new Api2Query();
        api2Query.setGeneralTerms(query);
        if (queryFilter != null && !queryFilter.isEmpty()) {
            String[] filters = queryFilter.split("&");
            for (String filter : filters) {
                api2Query.addQueryRefinement(filter);
            }
        }
        api2Query.setProfile(searchApiProfile);
        return api2Query;
    }
}
