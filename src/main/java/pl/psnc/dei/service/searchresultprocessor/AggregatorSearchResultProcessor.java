package pl.psnc.dei.service.searchresultprocessor;

import pl.psnc.dei.schema.search.SearchResult;

public interface AggregatorSearchResultProcessor {

	String DATA_UNAVAILABLE_VALUE = "Data unavailable";

	SearchResult fillMissingDataAndValidate(SearchResult searchResult, boolean onlyIiif, boolean onlyNotImported);
}
