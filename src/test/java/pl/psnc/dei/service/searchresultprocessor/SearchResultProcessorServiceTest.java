package pl.psnc.dei.service.searchresultprocessor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.util.TransferPossibility;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class SearchResultProcessorServiceTest {

	private RecordTransferValidationCache recordTransferValidationCache = new RecordTransferValidationCache();

	@Mock
	private EuropeanaSearchResultProcessor europeanaSearchResultProcessor;

	@InjectMocks
	private SearchResultProcessorService searchResultProcessorService = new SearchResultProcessorService(europeanaSearchResultProcessor, recordTransferValidationCache);

	private void setup() {
		when(europeanaSearchResultProcessor.fillMissingDataAndValidate(any(SearchResult.class))).thenAnswer(a -> {
			SearchResult argument = a.getArgument(0);
			argument.setFormat("image/jpeg");
			argument.setTransferPossibility(TransferPossibility.POSSIBLE);
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
		SearchResult filledSearchResult = searchResultProcessorService.fillMissingDataAndValidate(Aggregator.EUROPEANA.getId(), searchResult);

		verify(europeanaSearchResultProcessor, times(1)).fillMissingDataAndValidate(any());
		Assert.assertEquals(filledSearchResult, searchResult);
	}

	@Test
	public void fillUnknownAggregatorResult() {
		setup();
		SearchResult searchResult = getSearchResult();
		SearchResult filledSearchResult = searchResultProcessorService.fillMissingDataAndValidate(Aggregator.UNKNOWN.getId(), searchResult);

		verifyZeroInteractions(europeanaSearchResultProcessor);
		Assert.assertEquals(filledSearchResult, searchResult);
		Assert.assertNull(filledSearchResult.getFormat());
		Assert.assertNull(filledSearchResult.getTransferPossibility());
	}

	@Test
	public void fillInvalidAggregatorResult() {
		setup();
		SearchResult searchResult = getSearchResult();
		SearchResult filledSearchResult = searchResultProcessorService.fillMissingDataAndValidate(123456789, searchResult);

		verifyZeroInteractions(europeanaSearchResultProcessor);
		Assert.assertEquals(filledSearchResult, searchResult);
		Assert.assertNull(filledSearchResult.getFormat());
		Assert.assertNull(filledSearchResult.getTransferPossibility());
	}
}
