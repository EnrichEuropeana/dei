package pl.psnc.dei.service.searchresultprocessor;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.RecordTransferValidationCache;

@Service
public class SearchResultProcessorService {

	private RecordTransferValidationCache recordTransferValidationCache;

	private EuropeanaSearchResultProcessor europeanaSearchResultProcessor;

	public SearchResultProcessorService(EuropeanaSearchResultProcessor europeanaSearchResultProcessor,
										RecordTransferValidationCache recordTransferValidationCache) {
		this.europeanaSearchResultProcessor = europeanaSearchResultProcessor;
		this.recordTransferValidationCache = recordTransferValidationCache;
	}

	public SearchResult fillMissingDataAndValidate(int aggregatorId, SearchResult searchResult) {
		Aggregator aggregator = Aggregator.getById(aggregatorId);

		switch (aggregator) {
			case EUROPEANA:
				return europeanaSearchResultProcessor.fillMissingDataAndValidate(searchResult);
			case DDB:
				//todo implement
			default:
				return searchResult;
		}
	}

	public void clearCache() {
		recordTransferValidationCache.clear();
	}
}
