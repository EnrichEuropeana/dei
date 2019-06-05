package pl.psnc.dei.service.searchresultprocessor;

import pl.psnc.dei.schema.search.SearchResult;

public interface AggregatorSearchResultProcessor {

	SearchResult fillMissingDataAndValidate(SearchResult searchResult);
}
