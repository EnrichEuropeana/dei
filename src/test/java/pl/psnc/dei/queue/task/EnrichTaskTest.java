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
import pl.psnc.dei.model.TranscriptionType;
import pl.psnc.dei.model.factory.TranscriptionFactory;
import pl.psnc.dei.service.EnrichmentNotifierService;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.TranscriptionConverter;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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

    @Autowired
    private ContextMediator contextMediator;

    @Mock
    private Map<TranscriptionType, TranscriptionFactory> transcriptionFactories;


    //    @Qualifier("transcriptionPlatformService")
//    @Autowired
//    private TranscriptionPlatformService tps;
    @Autowired
    private TranscriptionConverter tc;

    @Mock
    // mocked as Transcribathon dev platform not always work, sometimes drop records, or returns 5xx codes
    private TranscriptionPlatformService tps;
    private EnrichTask enrichTask;

    @Mock
    private EnrichmentNotifierService ens;

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
        doNothing().when(ens).notifyPublishers(any());
    }

    @Before
    public void init() {
        this.record = new Record();
        this.record.setIdentifier("/2020601/https___1914_1918_europeana_eu_contributions_17173");
        this.record.setTranscriptions(new ArrayList<>());
        this.record.setState(Record.RecordState.E_PENDING);
        this.qrs.saveRecord(this.record);
        this.enrichTask = new EnrichTask(this.record, this.qrs, this.tps, this.ess, this.eas, this.contextMediator,
                this.tc, this.transcriptionFactories);
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

        EnrichTask enrichTask = new EnrichTask(record, qrs, tps, ess, eas, contextMediator, tc,
                this.transcriptionFactories);
        enrichTask.process();
        assertTrue(
                this.transcriptionRepository.findByTpId(transcription.getTpId()).isEmpty()
        );
    }

    @Test
    @Rollback
    @Transactional
    public void whenPostedTwice_notDuplicateRecords() {
        // if post method is called this task will be created
        EnrichTask enrichTask = new EnrichTask(record, qrs, tps, ess, eas, contextMediator, tc,
                this.transcriptionFactories);
        enrichTask.process();
        enrichTask.process();
        List<Transcription> transcriptionsFound = this.transcriptionRepository.findAllByTpId("203544");
        assertEquals(1, transcriptionsFound.size());
    }
}
