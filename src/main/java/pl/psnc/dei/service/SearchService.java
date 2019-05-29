package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.Item;
import pl.psnc.dei.response.search.SearchResponse;

import java.util.Map;

@Service
public class SearchService {

	private EuropeanaSearchService europeanaSearchService;

	public SearchService(EuropeanaSearchService europeanaSearchService) {
		this.europeanaSearchService = europeanaSearchService;
	}

	public SearchResponse<Facet, Item> search(int aggregatorId, String query, Map<String, String> requestParams) {
		Aggregator aggregator = Aggregator.getById(aggregatorId);

		switch (aggregator) {
			case EUROPEANA:
				return europeanaSearchService.search(query, requestParams).block();
			case DDB:
				//todo implement DDB search
			default:
				return null;
		}
	}
}
