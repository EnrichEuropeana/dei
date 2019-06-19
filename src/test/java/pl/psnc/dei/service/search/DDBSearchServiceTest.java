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
import pl.psnc.dei.response.search.SearchResponse;
import pl.psnc.dei.response.search.ddb.DDBFacet;
import pl.psnc.dei.response.search.ddb.DDBFacetField;
import pl.psnc.dei.response.search.ddb.DDBItem;
import pl.psnc.dei.response.search.ddb.DDBSearchResponse;
import pl.psnc.dei.schema.search.DDBOffsetPagination;
import pl.psnc.dei.schema.search.Pagination;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;
import static pl.psnc.dei.util.DDBConstants.OFFSET_PARAM_NAME;
import static pl.psnc.dei.util.DDBConstants.ROWS_PARAM_NAME;

@RunWith(SpringRunner.class)
public class DDBSearchServiceTest {

	@Mock
	private WebClient webTestClient;

	@InjectMocks
	private DDBSearchService ddbSearchService = new DDBSearchService(WebClient.builder());

	private void mockWebClientResponse(final DDBSearchResponse resp) {
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
		when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<DDBSearchResponse>>notNull()))
				.thenReturn(Mono.just(resp));
	}

	private DDBSearchResponse getResponseOK() {
		DDBSearchResponse searchResponse = new DDBSearchResponse();
		searchResponse.setItemsCount(1);
		searchResponse.setTotalResults(1);
		searchResponse.setPagination(new DDBOffsetPagination());
		searchResponse.setItems(new ArrayList<>());
		DDBItem item = new DDBItem();
		item.setId("1");
		searchResponse.getItems().add(item);
		searchResponse.setFacets(new ArrayList<>());
		DDBFacet facet = new DDBFacet();
		facet.setName("a");
		facet.setFields(new ArrayList<>());
		DDBFacetField field = new DDBFacetField();
		field.setCount(1);
		field.setLabel("label");
		facet.getFields().add(field);
		searchResponse.getFacets().add(facet);

		return searchResponse;
	}

	@Test
	public void searchNoParams() {
		DDBSearchResponse responseOK = getResponseOK();
		mockWebClientResponse(responseOK);
		int rowsPerPage = 10;

		Mono<SearchResponse> response = ddbSearchService.search("", new HashMap<>(), rowsPerPage);

		Assert.assertNotNull(response);
		Assert.assertEquals(response.block(), responseOK);

		Pagination responsePagination = response.block().getPagination();
		Assert.assertTrue(responsePagination instanceof DDBOffsetPagination);

		Map<String, String> paginationParams = responsePagination.getRequestParams();

		Assert.assertEquals(String.valueOf(rowsPerPage), paginationParams.get(ROWS_PARAM_NAME));
		Assert.assertEquals(String.valueOf(0 + rowsPerPage), paginationParams.get(OFFSET_PARAM_NAME));
	}

	@Test
	public void searchOffsetParams() {
		DDBSearchResponse responseOK = getResponseOK();
		mockWebClientResponse(responseOK);
		int rowsPerPage = 10;
		int offset = 10;
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put(OFFSET_PARAM_NAME, String.valueOf(offset));

		Mono<SearchResponse> response = ddbSearchService.search("", requestParams, rowsPerPage);

		Assert.assertNotNull(response);
		Assert.assertEquals(response.block(), responseOK);

		Pagination responsePagination = response.block().getPagination();
		Assert.assertTrue(responsePagination instanceof DDBOffsetPagination);

		Map<String, String> paginationParams = responsePagination.getRequestParams();

		Assert.assertEquals(String.valueOf(rowsPerPage), paginationParams.get(ROWS_PARAM_NAME));
		Assert.assertEquals(String.valueOf(offset + rowsPerPage), paginationParams.get(OFFSET_PARAM_NAME));
	}
}
