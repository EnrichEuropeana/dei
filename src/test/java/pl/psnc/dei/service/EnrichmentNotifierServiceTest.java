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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class EnrichmentNotifierServiceTest {

    private final String RECORD_ID = "/0940442/_nnm6mST";

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
        Mockito.verify(enrichmentNotifierService, times(1))
                .sendNotification(any(), any());
    }

    private JsonObject getRecordFromTestResources(String path) throws IOException {
        Resource resource = new ClassPathResource("records" + path + ".json");
        return JSON.parseAny(resource.getInputStream()).getAsObject();
    }
}