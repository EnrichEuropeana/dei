package pl.psnc.dei.service;

import lombok.SneakyThrows;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class EnrichmentNotifierServiceTest {

    private final String RECORD_ID = "/401/item_66AW2RSI5V5KVHVIEB2VZAG2L7WYCRI2";

    @InjectMocks
    @Spy
    EnrichmentNotifierService enrichmentNotifierService;

    @Mock
    private EuropeanaSearchService europeanaSearchService;

    @Before
    public void setUp() {
        when(europeanaSearchService.retrieveRecordInJson(anyString()))
                .thenAnswer(invocationOnMock -> {
                    String recordId = invocationOnMock.getArgument(0);
                    return getRecordFromTestResources(recordId);
                });
    }

    @Test
    @SneakyThrows
    public void shouldSendNotifications() {
        Record record = new Record();
        record.setIdentifier(RECORD_ID);

        enrichmentNotifierService.notifyPublishers(record);
        Mockito.verify(enrichmentNotifierService, times(2))
                .sendNotification(any(), any(), any());
    }

    @SneakyThrows
    @Test
    public void shouldExtractNationalAggregatorUrl() {
        JsonObject jsonObject = getRecordFromTestResources(RECORD_ID);
        URL url = enrichmentNotifierService.extractNationalAggregatorUrl(jsonObject);
        assertEquals(new URL("http://www.e-varamu.ee/item/66AW2RSI5V5KVHVIEB2VZAG2L7WYCRI2"), url);
    }

    @SneakyThrows
    @Test
    public void shouldExtractContentProviderUrl() {
        JsonObject jsonObject = getRecordFromTestResources(RECORD_ID);
        List<URL> urls = enrichmentNotifierService.extractContentProviderUrls(jsonObject);
        assertEquals(1, urls.size());
        assertEquals(new URL("https://www.muis.ee/digitaalhoidla/api/meedia/originaal?id=c97d0c2d-ac38-4d1b-9b75-3ac5fcc55d7b"), urls.get(0));
    }

    @SneakyThrows
    @Test
    public void shouldExtractOAIIdentifier() {
        JsonObject jsonObject = getRecordFromTestResources(RECORD_ID);
        String oai = enrichmentNotifierService.extractOai(jsonObject);
        assertEquals("oai:muis.ee:1118596", oai);
    }

    private JsonObject getRecordFromTestResources(String path) throws IOException {
        Resource resource = new ClassPathResource("records" + path + ".json");
        return JSON.parseAny(resource.getInputStream()).getAsObject();
    }
}