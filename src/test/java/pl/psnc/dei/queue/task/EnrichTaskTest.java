package pl.psnc.dei.queue.task;

import lombok.SneakyThrows;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.DAO.TranscriptionRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
public class EnrichTaskTest {
    private JsonArray transcribathonResponse;
    private Record record;

    @Value("classpath:queue/enrich-response.json")
    private Resource transcriptionResponseResource;

    @Autowired
    private QueueRecordService qrs;

    @Autowired
    private EuropeanaSearchService ess;

    @Autowired
    private EuropeanaAnnotationsService eas;

    @Autowired
    private TranscriptionRepository transcriptionRepository;

//    @Qualifier("transcriptionPlatformService")
//    @Autowired
//    private TranscriptionPlatformService tps;

    @Mock
    // mocked as Transcribathon dev platform not always work, sometimes drop records, or returns 5xx codes
    private TranscriptionPlatformService tps;

    @SneakyThrows
    @Before
    public void readJson() {
        this.transcribathonResponse = JSON.parseAny(
                new FileInputStream(this.transcriptionResponseResource.getFile())
        ).getAsArray();
    }

    @Before
    public void prepareMock() {
        when(tps.fetchTranscriptionsFor(any())).thenReturn(this.transcribathonResponse);
    }

    @Before
    public void init() {
        this.record = new Record();
        this.record.setIdentifier("/2020601/https___1914_1918_europeana_eu_contributions_17173");
        this.record.setTranscriptions(new ArrayList<>());
        this.qrs.saveRecord(this.record);
    }

    @SneakyThrows
    @Test
    @Transactional
    @Rollback
    public void canRemoveMissingTranscriptions() {
        // add spare transcription
        // this one should be deleted after enrich task was fired
        Transcription transcription = new Transcription();
        transcription.setTranscriptionContent(new JsonObject());
        transcription.setRecord(this.record);
        transcription.setTpId("12343");
        transcription.setAnnotationId("sdg");
        transcription = this.transcriptionRepository.save(transcription);
        this.record.getTranscriptions().add(transcription);

        this.record = this.qrs.getRecord(this.record.getIdentifier());

        EnrichTask enrichTask = new EnrichTask(this.record, this.qrs, this.tps, this.ess, this.eas);
        enrichTask.process();
        assertTrue(
                this.transcriptionRepository.findByTpId(transcription.getTpId()).isEmpty()
        );
    }

    @Test
    @Rollback
    public void whenPostedTwice_notDuplicateRecords() {
        // if post method is called this task will be created
        EnrichTask enrichTask = new EnrichTask(this.record, this.qrs, this.tps, this.ess, this.eas);
        enrichTask.process();
        enrichTask.process();
        List<Transcription> transcriptionsFound = this.transcriptionRepository.findAllByTpId("203544");
        assertEquals(1, transcriptionsFound.size());
    }
}
