package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.DDBFormatResolver;
import pl.psnc.dei.service.RecordDataCache;
import pl.psnc.dei.service.search.DDBSearchService;
import pl.psnc.dei.util.IiifAvailability;

import static org.mockito.Mockito.*;
import static pl.psnc.dei.service.searchresultprocessor.AggregatorSearchResultProcessor.DATA_UNAVAILABLE_VALUE;

@RunWith(SpringRunner.class)
public class DDBSearchResultProcessorTest {

	private final JsonObject ddbRestResponse = JSON.parse("{\"@graph\":[{\"@id\":\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/ADIKPB25JLXQ7B4S4OQYTBOZV2OUM5FS\",\"@type\":\"edm:Event\",\"hasType\":\"http://terminology.lido-schema.org/lido00228\",\"occuredAt\":[\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/HZUHR23UC2I3JSW6YSGLIQPOVDEIU2FZ\",\"http://ddb.vocnet.org/zeitvokabular/dat00002\",\"http://ddb.vocnet.org/zeitvokabular/dat00001\"]},{\"@id\":\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/GW7ZPYXFKMUIGLTXQFXSUPVCJTIUNYPG\",\"@type\":\"edm:Event\",\"P11_had_participant\":\"http://d-nb.info/gnd/129005398\",\"hasType\":\"http://terminology.lido-schema.org/lido00012\"},{\"@id\":\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/HZUHR23UC2I3JSW6YSGLIQPOVDEIU2FZ\",\"@type\":\"edm:TimeSpan\",\"begin\":\"2004\",\"end\":\"2004\"},{\"@id\":\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/L62HEZBWVJVA7HDRE44W7USL6R63VUDU\",\"@type\":\"skos:Concept\",\"prefLabel\":\"Monografie\"},{\"@id\":\"http://d-nb.info/971093954\",\"@type\":\"edm:WebResource\",\"type\":\"http://ddb.vocnet.org/medientyp/mt003\",\"rights\":\"http://www.deutsche-digitale-bibliothek.de/lizenzen/rv-fz/\",\"edm:rights\":{\"@id\":\"http://www.europeana.eu/rights/rr-f/\"}},{\"@id\":\"http://d-nb.info/DE-101/971093954\",\"@type\":\"ore:Aggregation\",\"rights\":\"http://creativecommons.org/publicdomain/zero/1.0/\",\"aggregatedCHO\":\"ns4:/WFLISQEWLQLKB6GIDKIY5X43KL4YTS3A\",\"dataProvider\":\"http://ld.zdb-services.de/data/organisations/DE-101\",\"edm:dataProvider\":\"Deutsche Nationalbibliothek\",\"isShownAt\":\"http://d-nb.info/971093954\",\"provider\":\"Deutsche Digitale Bibliothek\",\"edm:rights\":{\"@id\":\"http://www.europeana.eu/rights/rr-f/\"}},{\"@id\":\"http://d-nb.info/ddc-sg/530\",\"@type\":\"skos:Concept\",\"prefLabel\":\"Physik\"},{\"@id\":\"http://d-nb.info/gnd/129005398\",\"@type\":\"edm:Agent\",\"wasPresentAt\":\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/GW7ZPYXFKMUIGLTXQFXSUPVCJTIUNYPG\",\"prefLabel\":\"Hellerer, Thomas\"},{\"@id\":\"http://d-nb.info/gnd/4039238-7\",\"@type\":\"skos:Concept\",\"prefLabel\":\"Mikroskopie\"},{\"@id\":\"http://d-nb.info/gnd/4235441-9\",\"@type\":\"skos:Concept\",\"prefLabel\":\"CARS-Spektroskopie\"},{\"@id\":\"http://ddb.vocnet.org/medientyp/mt003\",\"@type\":\"skos:Concept\",\"notation\":\"mediatype_003\"},{\"@id\":\"http://ddb.vocnet.org/sparte/sparte002\",\"@type\":\"skos:Concept\",\"notation\":\"sec_02\"},{\"@id\":\"http://ddb.vocnet.org/zeitvokabular/dat00001\",\"@type\":\"edm:TimeSpan\",\"notation\":\"time_62100\"},{\"@id\":\"http://ddb.vocnet.org/zeitvokabular/dat00002\",\"@type\":\"edm:TimeSpan\",\"notation\":\"time_62110\"},{\"@id\":\"http://id.loc.gov/vocabulary/iso639-2/ger\",\"@type\":\"dcterms:LinguisticSystem\",\"value\":\"ger\"},{\"@id\":\"http://ld.zdb-services.de/data/organisations/DE-101\",\"@type\":\"edm:Agent\",\"ns3:type\":{\"@id\":\"http://ddb.vocnet.org/sparte/sparte002\"},\"prefLabel\":\"Deutsche Nationalbibliothek\"},{\"@id\":\"ns4:/WFLISQEWLQLKB6GIDKIY5X43KL4YTS3A\",\"@type\":\"edm:ProvidedCHO\",\"contributor\":\"Hellerer, Thomas\",\"description\":\"München, Univ., Diss., 2004\",\"identifier\":[\"(DE-599)DNB971093954\",\"http://nbn-resolving.de/urn:nbn:de:bvb:19-19576\",\"(OCoLC)723333326\"],\"language\":\"ger\",\"dc:subject\":[\"Online-Publikation\",\"CARS-Spektroskopie\",\"Physik\",\"Mikroskopie\"],\"title\":\"CARS-Mikroskopie :Entwicklung und Anwendung\",\"dc:type\":\"Monografie\",\"dcterms:language\":{\"@id\":\"http://id.loc.gov/vocabulary/iso639-2/ger\"},\"subject\":[\"http://d-nb.info/gnd/4235441-9\",\"http://d-nb.info/gnd/4039238-7\",\"http://d-nb.info/ddc-sg/530\"],\"aggregationEntity\":\"false\",\"hierarchyPosition\":\"-1\",\"hierarchyType\":\"htype_021\",\"hasMet\":[\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/GW7ZPYXFKMUIGLTXQFXSUPVCJTIUNYPG\",\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/ADIKPB25JLXQ7B4S4OQYTBOZV2OUM5FS\"],\"hasType\":\"file:///C:/Users/Kamil%20Gronowski/Desktop/EnrichEuropeana-dei/dei/RDF/L62HEZBWVJVA7HDRE44W7USL6R63VUDU\",\"edm:type\":\"TEXT\"}],\"@context\":{\"prefLabel\":{\"@id\":\"http://www.w3.org/2004/02/skos/core#prefLabel\"},\"value\":{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#value\"},\"P11_had_participant\":{\"@id\":\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#P11_had_participant\",\"@type\":\"@id\"},\"hasType\":{\"@id\":\"http://www.europeana.eu/schemas/edm/hasType\",\"@type\":\"@id\"},\"notation\":{\"@id\":\"http://www.w3.org/2004/02/skos/core#notation\"},\"rights\":{\"@id\":\"http://purl.org/dc/terms/rights\",\"@type\":\"@id\"},\"type\":{\"@id\":\"http://purl.org/dc/elements/1.1/type\",\"@type\":\"@id\"},\"subject\":{\"@id\":\"http://purl.org/dc/terms/subject\",\"@type\":\"@id\"},\"hierarchyPosition\":{\"@id\":\"http://www.deutsche-digitale-bibliothek.de/edm/hierarchyPosition\"},\"identifier\":{\"@id\":\"http://purl.org/dc/elements/1.1/identifier\"},\"language\":{\"@id\":\"http://purl.org/dc/elements/1.1/language\"},\"hierarchyType\":{\"@id\":\"http://www.deutsche-digitale-bibliothek.de/edm/hierarchyType\"},\"hasMet\":{\"@id\":\"http://www.europeana.eu/schemas/edm/hasMet\",\"@type\":\"@id\"},\"contributor\":{\"@id\":\"http://purl.org/dc/elements/1.1/contributor\"},\"description\":{\"@id\":\"http://purl.org/dc/elements/1.1/description\"},\"aggregationEntity\":{\"@id\":\"http://www.deutsche-digitale-bibliothek.de/edm/aggregationEntity\"},\"title\":{\"@id\":\"http://purl.org/dc/elements/1.1/title\"},\"end\":{\"@id\":\"http://www.europeana.eu/schemas/edm/end\"},\"begin\":{\"@id\":\"http://www.europeana.eu/schemas/edm/begin\"},\"provider\":{\"@id\":\"http://www.europeana.eu/schemas/edm/provider\"},\"isShownAt\":{\"@id\":\"http://www.europeana.eu/schemas/edm/isShownAt\",\"@type\":\"@id\"},\"dataProvider\":{\"@id\":\"http://www.europeana.eu/schemas/edm/dataProvider\",\"@type\":\"@id\"},\"aggregatedCHO\":{\"@id\":\"http://www.europeana.eu/schemas/edm/aggregatedCHO\",\"@type\":\"@id\"},\"wasPresentAt\":{\"@id\":\"http://www.europeana.eu/schemas/edm/wasPresentAt\",\"@type\":\"@id\"},\"occuredAt\":{\"@id\":\"http://www.europeana.eu/schemas/edm/occuredAt\",\"@type\":\"@id\"},\"@vocab\":\"http://www.deutsche-digitale-bibliothek.de/cortex\",\"item\":\"http://www.deutsche-digitale-bibliothek.de/item\",\"ore\":\"http://www.openarchives.org/ore/terms/\",\"ddb\":\"http://www.deutsche-digitale-bibliothek.de/edm/\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"source\":\"http://www.deutsche-digitale-bibliothek.de/ns/cortex-item-source\",\"ns2\":\"http://www.deutsche-digitale-bibliothek.de/institution\",\"ns4\":\"http://www.deutsche-digitale-bibliothek.de/item\",\"ns3\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"edm\":\"http://www.europeana.eu/schemas/edm/\",\"ns6\":\"http://www.deutsche-digitale-bibliothek.de/ns/cortex-item-source\",\"institution\":\"http://www.deutsche-digitale-bibliothek.de/institution\",\"ns5\":\"http://www.deutsche-digitale-bibliothek.de/ns/cortex-item-source-description\",\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"dcterms\":\"http://purl.org/dc/terms/\",\"source-description\":\"http://www.deutsche-digitale-bibliothek.de/ns/cortex-item-source-description\",\"dc\":\"http://purl.org/dc/elements/1.1/\",\"crm\":\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\"}}");

	@Mock
	private DDBSearchService ddbSearchService;

	@Mock
	private DDBFormatResolver ddbFormatResolver;

	private RecordDataCache recordDataCache = new RecordDataCache();

	@InjectMocks
	private DDBSearchResultProcessor ddbSearchResultProcessor = new DDBSearchResultProcessor(ddbSearchService, ddbFormatResolver, recordDataCache);

	private void setupGetMimeType() {
		when(ddbFormatResolver.getRecordFormat(anyString())).thenReturn("image/jpeg");
	}

	private void setupGetUnsupportedMimeType() {
		when(ddbFormatResolver.getRecordFormat(anyString())).thenReturn("image/gif");
	}

	private void setupGetNullMimeType() {
		when(ddbFormatResolver.getRecordFormat(anyString())).thenReturn(null);
	}

	private void setupGetMimeTypeException() {
		when(ddbFormatResolver.getRecordFormat(anyString())).thenThrow(new RuntimeException());
	}

	private void setupRestResponse() {
		when(ddbSearchService.retrieveRecordAndConvertToJsonLd(anyString())).thenReturn(ddbRestResponse);
	}

	private void setupServiceUnavailable() {
		when(ddbSearchService.retrieveRecordAndConvertToJsonLd(anyString())).thenThrow(new RuntimeException());
	}

	private SearchResult getSearchResult () {
		SearchResult searchResult = new SearchResult();
		searchResult.setId("1");
		searchResult.setTitle("test");
		return searchResult;
	}

	@Test
	public void fillRecordData() {
		setupGetMimeType();
		setupRestResponse();
		SearchResult searchResult = getSearchResult();

		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		Assert.assertEquals(searchResult, result);
		Assert.assertEquals("image/jpeg", result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals("Deutsche Nationalbibliothek", result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals("http://creativecommons.org/publicdomain/zero/1.0/", result.getLicense());
		//todo uncomment when DDB binary endpoint become available. For now IiifValidator always returns CONVERSION_IMPOSSIBLE for DDB
		//Assert.assertEquals(IiifAvailability.CONVERSION_POSSIBLE, result.getIiifAvailability());
	}

	@Test
	public void fillRecordDataNullMimeType() {
		setupGetNullMimeType();
		setupRestResponse();
		SearchResult searchResult = getSearchResult();

		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		Assert.assertEquals(searchResult, result);
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals("Deutsche Nationalbibliothek", result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals("http://creativecommons.org/publicdomain/zero/1.0/", result.getLicense());
		Assert.assertEquals(IiifAvailability.CONVERSION_IMPOSSIBLE, result.getIiifAvailability());
	}

	@Test
	public void fillRecordDataUnsupportedMimeType() {
		setupGetUnsupportedMimeType();
		setupRestResponse();
		SearchResult searchResult = getSearchResult();

		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		Assert.assertEquals(searchResult, result);
		Assert.assertEquals("image/gif", result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals("Deutsche Nationalbibliothek", result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals("http://creativecommons.org/publicdomain/zero/1.0/", result.getLicense());
		Assert.assertEquals(IiifAvailability.CONVERSION_IMPOSSIBLE, result.getIiifAvailability());
	}

	@Test
	public void fillRecordDataMimeTypeException() {
		setupGetMimeTypeException();
		setupRestResponse();
		SearchResult searchResult = getSearchResult();

		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		Assert.assertEquals(searchResult, result);
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals("Deutsche Nationalbibliothek", result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals("http://creativecommons.org/publicdomain/zero/1.0/", result.getLicense());
		Assert.assertEquals(IiifAvailability.CONVERSION_IMPOSSIBLE, result.getIiifAvailability());
	}

	@Test
	public void fillRecordDataRestException() {
		setupGetMimeType();
		setupServiceUnavailable();
		SearchResult searchResult = getSearchResult();

		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		Assert.assertEquals(searchResult, result);
		Assert.assertEquals("image/jpeg", result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLicense());
		Assert.assertEquals(IiifAvailability.DATA_UNAVAILABLE, result.getIiifAvailability());
	}

	@Test
	public void fillRecordDataRestExceptionNullMimeType() {
		setupGetNullMimeType();
		setupServiceUnavailable();
		SearchResult searchResult = getSearchResult();

		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		Assert.assertEquals(searchResult, result);
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLicense());
		Assert.assertEquals(IiifAvailability.DATA_UNAVAILABLE, result.getIiifAvailability());
	}

	@Test
	public void fillRecordDataRestExceptionMimeTypeException() {
		setupGetMimeTypeException();
		setupServiceUnavailable();
		SearchResult searchResult = getSearchResult();

		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		Assert.assertEquals(searchResult, result);
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLicense());
		Assert.assertEquals(IiifAvailability.DATA_UNAVAILABLE, result.getIiifAvailability());
	}

	@Test
	public void fillDataFromCache() {
		setupGetMimeType();
		setupRestResponse();
		SearchResult searchResult = getSearchResult();
		recordDataCache.addValidationResult(searchResult.getId(), "image/jpeg", IiifAvailability.CONVERSION_POSSIBLE);
		recordDataCache.addValue(searchResult.getId(), "author", DATA_UNAVAILABLE_VALUE);
		recordDataCache.addValue(searchResult.getId(), "provider", "Deutsche Nationalbibliothek");
		recordDataCache.addValue(searchResult.getId(), "language", DATA_UNAVAILABLE_VALUE);
		recordDataCache.addValue(searchResult.getId(), "license", "http://creativecommons.org/publicdomain/zero/1.0/");
		SearchResult result = ddbSearchResultProcessor.fillMissingDataAndValidate(searchResult, false, false);

		verifyZeroInteractions(ddbSearchService);
		Assert.assertEquals(searchResult, result);
		Assert.assertEquals("image/jpeg", result.getFormat());
		Assert.assertEquals("test", result.getTitle());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getAuthor());
		Assert.assertEquals("Deutsche Nationalbibliothek", result.getProvider());
		Assert.assertEquals(DATA_UNAVAILABLE_VALUE, result.getLanguage());
		Assert.assertEquals("http://creativecommons.org/publicdomain/zero/1.0/", result.getLicense());
		Assert.assertEquals(IiifAvailability.CONVERSION_POSSIBLE, result.getIiifAvailability());
	}
}
