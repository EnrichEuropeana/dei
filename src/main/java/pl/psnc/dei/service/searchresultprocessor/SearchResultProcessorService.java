package pl.psnc.dei.service.searchresultprocessor;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.RecordDataCache;

@Service
public class SearchResultProcessorService {

	private RecordDataCache recordDataCache;

	private EuropeanaSearchResultProcessor europeanaSearchResultProcessor;

	private DDBSearchResultProcessor ddbSearchResultProcessor;

	public SearchResultProcessorService(EuropeanaSearchResultProcessor europeanaSearchResultProcessor,
										DDBSearchResultProcessor ddbSearchResultProcessor,
										RecordDataCache recordDataCache) {
		this.europeanaSearchResultProcessor = europeanaSearchResultProcessor;
		this.ddbSearchResultProcessor = ddbSearchResultProcessor;
		this.recordDataCache = recordDataCache;
	}

	public SearchResult fillMissingDataAndValidate(int aggregatorId, SearchResult searchResult, boolean onlyIiif) {
		Aggregator aggregator = Aggregator.getById(aggregatorId);

		switch (aggregator) {
			case EUROPEANA:
				return europeanaSearchResultProcessor.fillMissingDataAndValidate(searchResult, onlyIiif);
			case DDB:
				return ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, onlyIiif);
			default:
				return searchResult;
		}
	}

	public void clearCache() {
		recordDataCache.clear();
	}
}
