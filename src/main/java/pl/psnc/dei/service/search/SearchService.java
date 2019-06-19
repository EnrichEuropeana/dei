package pl.psnc.dei.service.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.Item;
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.schema.search.SearchResults;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {

	private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

	private EuropeanaSearchService europeanaSearchService;
	private DDBSearchService ddbSearchService;

	public SearchService(EuropeanaSearchService europeanaSearchService, DDBSearchService ddbSearchService) {
		this.europeanaSearchService = europeanaSearchService;
		this.ddbSearchService = ddbSearchService;
	}

	public SearchResults search(int aggregatorId, String query, Map<String, String> requestParams, int rowsPerPage) {
		Aggregator aggregator = Aggregator.getById(aggregatorId);

		SearchResponse<Facet, Item> response;

		try {
			switch (aggregator) {
				case EUROPEANA:
					response = europeanaSearchService.search(query, requestParams, rowsPerPage).block();
					break;
				case DDB:
					response = ddbSearchService.search(query, requestParams, rowsPerPage).block();
					break;
				default:
					return null;
			}
		} catch (Exception e) {
			logger.warn("Error during search request to external api.", e);
			return null;
		}

		return prepareSearchResults(response);
	}

	private SearchResults prepareSearchResults(SearchResponse<Facet, Item> response) {

		SearchResults searchResults = new SearchResults();
		searchResults.setDefaultPagination(response.getDefaultPagination());
		searchResults.setNextPagination(response.getPagination());
		searchResults.setFacets(response.getFacets());
		Integer totalResults = response.getTotalResults();
		searchResults.setTotalResults(totalResults != null ? totalResults : 0);
		Integer itemsCount = response.getItemsCount();
		searchResults.setResultsCollected(itemsCount != null ? itemsCount : 0);

		List<Item> items = response.getItems();
		items.forEach(item -> searchResults.getResults().add(itemToSearchResult(item)));

		return searchResults;
	}

	/**
	 * Create a SearchResult object from EuropeanaItem which is retrieved from the response
	 *
	 * @param item item found in the results
	 * @return item converted to search result object
	 */
	private SearchResult itemToSearchResult(Item item) {
		SearchResult searchResult = new SearchResult();

		//id
		searchResult.setId(item.getId());

		// title
		if (item.getTitle() != null && !item.getTitle().isEmpty()) {
			searchResult.setTitle(item.getTitle().get(0));
		}

		// author
		if (item.getAuthor() != null && !item.getAuthor().isEmpty()) {
			searchResult.setAuthor(item.getAuthor());
		}

		// issued

		// provider institution
		if (item.getDataProviderInstitution() != null && !item.getDataProviderInstitution().isEmpty()) {
			searchResult.setProvider(item.getDataProviderInstitution());
		}

		// format
		if (item.getFormat() != null && !item.getFormat().isEmpty()) {
			searchResult.setFormat(item.getFormat());
		}

		// language
		if (item.getLanguage() != null && !item.getLanguage().isEmpty()) {
			searchResult.setLanguage(item.getLanguage().get(0));
		}

		// license
		if (item.getRights() != null && !item.getRights().isEmpty()) {
			searchResult.setLicense(item.getRights().get(0));
		}

		// image URL
		if (item.getThumbnailURL() != null && !item.getThumbnailURL().isEmpty()) {
			searchResult.setImageURL(item.getThumbnailURL());
		}

		// source object URL
		if (item.getSourceObjectURL() != null && !item.getSourceObjectURL().isEmpty()) {
			searchResult.setSourceObjectURL(item.getSourceObjectURL());
		}

		return searchResult;
	}
}
