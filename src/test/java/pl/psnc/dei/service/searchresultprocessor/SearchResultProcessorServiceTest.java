package pl.psnc.dei.service.searchresultprocessor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.RecordDataCache;
import pl.psnc.dei.util.IiifAvailability;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class SearchResultProcessorServiceTest {

	private RecordDataCache recordDataCache = new RecordDataCache();

	@Mock
	private EuropeanaSearchResultProcessor europeanaSearchResultProcessor;

	@Mock
	private DDBSearchResultProcessor ddbSearchResultProcessor;

	@InjectMocks
	private SearchResultProcessorService searchResultProcessorService = new SearchResultProcessorService(europeanaSearchResultProcessor, ddbSearchResultProcessor, recordDataCache);

	private void setup() {
		when(europeanaSearchResultProcessor.fillMissingDataAndValidate(any(SearchResult.class), eq(false))).thenAnswer(a -> {
			SearchResult argument = a.getArgument(0);
			argument.setFormat("image/jpeg");
			argument.setIiifAvailability(IiifAvailability.AVAILABLE);
			return argument;
		});
	}

	private SearchResult getSearchResult () {
		SearchResult searchResult = new SearchResult();
		searchResult.setId("1");
		searchResult.setTitle("test");
		return searchResult;
	}

	@Test
	public void fillEuropeanaResult() {
		setup();
		SearchResult searchResult = getSearchResult();
		SearchResult filledSearchResult = searchResultProcessorService.fillMissingDataAndValidate(Aggregator.EUROPEANA.getId(), searchResult, false);

		verify(europeanaSearchResultProcessor, times(1)).fillMissingDataAndValidate(any(), eq(false));
		Assert.assertEquals(filledSearchResult, searchResult);
	}

	@Test
	public void fillUnknownAggregatorResult() {
		setup();
		SearchResult searchResult = getSearchResult();
		SearchResult filledSearchResult = searchResultProcessorService.fillMissingDataAndValidate(Aggregator.UNKNOWN.getId(), searchResult, false);

		verifyZeroInteractions(europeanaSearchResultProcessor);
		Assert.assertEquals(filledSearchResult, searchResult);
		Assert.assertNull(filledSearchResult.getFormat());
		Assert.assertNull(filledSearchResult.getIiifAvailability());
	}

	@Test
	public void fillInvalidAggregatorResult() {
		setup();
		SearchResult searchResult = getSearchResult();
		SearchResult filledSearchResult = searchResultProcessorService.fillMissingDataAndValidate(123456789, searchResult, false);

		verifyZeroInteractions(europeanaSearchResultProcessor);
		Assert.assertEquals(filledSearchResult, searchResult);
		Assert.assertNull(filledSearchResult.getFormat());
		Assert.assertNull(filledSearchResult.getIiifAvailability());
	}
}
