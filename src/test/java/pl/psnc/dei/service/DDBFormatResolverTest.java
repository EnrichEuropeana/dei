package pl.psnc.dei.service;

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
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DDBFormatResolverTest {

	private final String ddbMultipleBinariesResponse ="{\"binary\":[{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/jpeg\",\"@position\":\"1\",\"@primary\":\"true\",\"@ref\":\"b922f4f3-978d-4c7a-9b95-fda378ee7429\"},{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/jpeg\",\"@position\":\"2\",\"@primary\":\"false\",\"@ref\":\"a3c50ab7-d77b-4f03-9254-d725e7957cf4\"},{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/jpeg\",\"@position\":\"3\",\"@primary\":\"false\",\"@ref\":\"085d43da-7a52-4473-af6b-cb3aeac5358a\"},{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/jpeg\",\"@position\":\"4\",\"@primary\":\"false\",\"@ref\":\"8d49f479-9b33-4922-931c-44871f985798\"}]}";
	private final String ddbMultipleBinariesNotTheSameResponse ="{\"binary\":[{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/jpeg\",\"@position\":\"1\",\"@primary\":\"true\",\"@ref\":\"b922f4f3-978d-4c7a-9b95-fda378ee7429\"},{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/png\",\"@position\":\"2\",\"@primary\":\"false\",\"@ref\":\"a3c50ab7-d77b-4f03-9254-d725e7957cf4\"},{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/jpeg\",\"@position\":\"3\",\"@primary\":\"false\",\"@ref\":\"085d43da-7a52-4473-af6b-cb3aeac5358a\"},{\"@name\":\"Kirche\",\"@name2\":\"©BildarchivFotoMarburg\",\"@mimetype\":\"image/jpeg\",\"@position\":\"4\",\"@primary\":\"false\",\"@ref\":\"8d49f479-9b33-4922-931c-44871f985798\"}]}";
	private final String ddbSingleBinaryResponse = "{\"binary\":{\"@name\":\"SzenemitJürgenProchnow(rechts).Quelle:(DeutschesFilminstitut-DIF)\",\"@mimetype\":\"image/jpeg\",\"@position\":\"1\",\"@primary\":\"true\",\"@ref\":\"01521b1c-05a2-4c3b-ab1f-6abe279503de\"}}";

	@Mock
	private WebClient webTestClient;

	@InjectMocks
	private DDBFormatResolver ddbFormatResolver = new DDBFormatResolver(WebClient.builder());

	private void mockWebClientResponse(final String resp) {
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
		when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<String>>notNull()))
				.thenReturn(Mono.justOrEmpty(resp));
	}

	@Test
	public void getFormatSingleBinary() {
		mockWebClientResponse(ddbSingleBinaryResponse);

		String result = ddbFormatResolver.getRecordFormat("test");

		Assert.assertEquals("image/jpeg", result);
	}

	@Test
	public void getFormatMultipleBinaries() {
		mockWebClientResponse(ddbMultipleBinariesResponse);

		String result = ddbFormatResolver.getRecordFormat("test");

		Assert.assertEquals("image/jpeg", result);
	}

	@Test
	public void getFormatMultipleBinariesNotTheSame() {
		mockWebClientResponse(ddbMultipleBinariesNotTheSameResponse);

		String result = ddbFormatResolver.getRecordFormat("test");

		Assert.assertNull(result);
	}

	@Test
	public void getFormatNullBinaries() {
		mockWebClientResponse(null);

		String result = ddbFormatResolver.getRecordFormat("test");

		Assert.assertNull(result);
	}

	@Test
	public void getFormatEmptyBinaries() {
		mockWebClientResponse("");

		String result = ddbFormatResolver.getRecordFormat("test");

		Assert.assertNull(result);
	}

	@Test
	public void getFormatNullResponseBinary() {
		mockWebClientResponse("null");

		String result = ddbFormatResolver.getRecordFormat("test");

		Assert.assertNull(result);
	}
}
