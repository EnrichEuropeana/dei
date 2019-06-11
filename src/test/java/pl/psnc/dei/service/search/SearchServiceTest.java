package pl.psnc.dei.service.search;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.response.search.europeana.EuropeanaFacet;
import pl.psnc.dei.response.search.europeana.EuropeanaFacetField;
import pl.psnc.dei.response.search.europeana.EuropeanaItem;
import pl.psnc.dei.response.search.europeana.EuropeanaSearchResponse;
import pl.psnc.dei.schema.search.EuropeanaCursorPagination;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.schema.search.SearchResults;
import pl.psnc.dei.util.EuropeanaConstants;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SearchServiceTest {

	@Mock
	private WebClient webTestClient;

	@InjectMocks
	private EuropeanaSearchService europeanaSearchService = new EuropeanaSearchService(WebClient.builder());

	@InjectMocks
	private SearchService searchService = new SearchService(europeanaSearchService);

	private void mockWebClientResponse(final EuropeanaSearchResponse resp) {
		final WebClient.RequestHeadersUriSpec uriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
		final WebClient.RequestHeadersSpec headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
		final WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

		when(webTestClient.get()).thenReturn(uriSpecMock);
		when(uriSpecMock.uri(ArgumentMatchers.<Function<UriBuilder, URI>>notNull())).thenReturn(headersSpecMock);
		when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);
		when(headersSpecMock.headers(notNull())).thenReturn(headersSpecMock);
		when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
		when(responseSpecMock.onStatus(ArgumentMatchers.any(Predicate.class), ArgumentMatchers.any(Function.class)))
				.thenReturn(responseSpecMock);
		when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<EuropeanaSearchResponse>>notNull()))
				.thenReturn(Mono.just(resp));
	}

	private EuropeanaSearchResponse getEuropeanaResponseOK() {
		EuropeanaSearchResponse searchResponse = new EuropeanaSearchResponse();
		searchResponse.setApikey("api2demo");
		searchResponse.setRequestNumber(999);
		searchResponse.setSuccess(true);
		searchResponse.setItemsCount(1);
		searchResponse.setTotalResults(1);
		searchResponse.setNextCursor("abc");
		searchResponse.setItems(new ArrayList<>());
		EuropeanaItem item = new EuropeanaItem();
		item.setId("1");
		searchResponse.getItems().add(item);
		searchResponse.setFacets(new ArrayList<>());
		EuropeanaFacet facet = new EuropeanaFacet();
		facet.setName("a");
		facet.setFields(new ArrayList<>());
		EuropeanaFacetField field = new EuropeanaFacetField();
		field.setCount(1);
		field.setLabel("label");
		facet.getFields().add(field);
		searchResponse.getFacets().add(facet);

		return searchResponse;
	}

	private SearchResults getEuropeanaResultOK() {
		SearchResults results = new SearchResults();
		results.setDefaultPagination(new EuropeanaCursorPagination("10"));
		results.setNextPagination(new EuropeanaCursorPagination("abc"));

		results.setFacets(new ArrayList<>());
		EuropeanaFacet facet = new EuropeanaFacet();
		facet.setName("a");
		facet.setFields(new ArrayList<>());
		EuropeanaFacetField field = new EuropeanaFacetField();
		field.setCount(1);
		field.setLabel("label");
		facet.getFields().add(field);
		results.getFacets().add(facet);

		results.setTotalResults(1);
		results.setResultsCollected(1);


		SearchResult searchResult = new SearchResult();
		searchResult.setId("1");
		results.setResults(new ArrayList<>());
		results.getResults().add(searchResult);

		return results;
	}

	@Test
	public void searchInEuropeanaResultOK() {
		SearchResults europeanaResultOK = getEuropeanaResultOK();

		mockWebClientResponse(getEuropeanaResponseOK());
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put(EuropeanaConstants.CURSOR_PARAM_NAME, "*");
		requestParams.put("only_iiif", "true");
		SearchResults results = searchService.search(Aggregator.EUROPEANA.getId(), "abc", requestParams, 10);

		Assert.assertNotNull(results);
		Assert.assertEquals(results.getTotalResults(), europeanaResultOK.getTotalResults());
		Assert.assertEquals(results.getResultsCollected(), europeanaResultOK.getResultsCollected());
		Map<String, String> defaultPaginationParams = results.getDefaultPagination().getRequestParams();
		Assert.assertTrue(defaultPaginationParams.containsKey("cursor"));
		Assert.assertTrue(defaultPaginationParams.containsValue("*"));
		Map<String, String> nextPaginationParams = results.getNextPagination().getRequestParams();
		Assert.assertTrue(nextPaginationParams.containsKey("cursor"));
		Assert.assertTrue(nextPaginationParams.containsValue("abc"));
		Assert.assertEquals(results.getFacets().size(), europeanaResultOK.getFacets().size());
		Assert.assertEquals(results.getFacets().get(0).getName(), europeanaResultOK.getFacets().get(0).getName());
		Assert.assertEquals(results.getResults().size(), europeanaResultOK.getResults().size());
		Assert.assertEquals(results.getResults().get(0).getId(), europeanaResultOK.getResults().get(0).getId());
	}

	@Test
	public void searchInvalidAggregator() {
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put(EuropeanaConstants.CURSOR_PARAM_NAME, "*");
		requestParams.put("only_iiif", "true");

		SearchResults results = searchService.search(123456, "abc", requestParams, 10);

		Assert.assertNull(results);
	}

	@Test
	public void searchUnknownAggregator() {
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put(EuropeanaConstants.CURSOR_PARAM_NAME, "*");
		requestParams.put("only_iiif", "true");

		SearchResults results = searchService.search(Aggregator.UNKNOWN.getId(), "abc", requestParams, 10);

		Assert.assertNull(results);
	}
}
