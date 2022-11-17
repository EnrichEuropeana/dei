package pl.psnc.dei.queue.task;

import lombok.SneakyThrows;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.DAO.TranscriptionRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.EnrichmentNotifierService;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("integration")
@SpringBootTest
public class UpdateTaskTest {
    // update task could be created in two ways, in normal way -> as when it is created by endpoint
    // or for recovery after crash -> by queue

    // https://europeana.transcribathon.eu/documents/story/item/?item=1180085
    //  JOHN (NICKNAMED JACK) HENRY MALLETT'S TRAGIC DEATH
    private final String RECORD_IDENTIFIER = "/2020601/https___1914_1918_europeana_eu_contributions_17173";
    private final String TP_ID = "203544";
    private final String ANNOTATION_ID = "53622";
    @Value("classpath:queue/update-response.json")
    private org.springframework.core.io.Resource updateResponseJson;
    private Record record;
    private List<Transcription> transcriptionList;

    @Mock
    private TranscriptionPlatformService tps;

    @Mock
    private EnrichmentNotifierService ens;

    @Autowired
    private TranscriptionRepository transcriptionRepository;

    @Autowired
    private RecordsRepository recordsRepository;

    @Autowired
    private QueueRecordService qrs;

    @Autowired
    private EuropeanaSearchService ess;

    @Mock
    private EuropeanaAnnotationsService eas;

    @Autowired
    private ContextMediator contextMediator;

    @SneakyThrows
    private void prepareTpsMock() {
        JsonObject response = JSON.parse(this.updateResponseJson.getInputStream());
        when(this.tps.fetchTranscriptionUpdate(any())).thenReturn(response);
    }

    private void prepareEasMock() {
        when(this.eas.updateTranscription(any(), any())).thenReturn("21");
    }

    @SneakyThrows
    private void initTranscriptions() {
        this.transcriptionList = new ArrayList<>();
        Transcription transcription = new Transcription();
        transcription.setTpId(this.TP_ID);
        transcription.setAnnotationId(this.ANNOTATION_ID);
        transcription.setTranscriptionContent(JSON.parse(this.updateResponseJson.getInputStream()));
        transcription = this.transcriptionRepository.save(transcription);
        this.transcriptionList.add(transcription);
    }

    private void initRecord() {
        this.record = new Record();
        this.record.setId(93274589L);
        this.record.setIdentifier(this.RECORD_IDENTIFIER);
        this.record.setState(Record.RecordState.U_PENDING);
        this.record.setTranscriptions(this.transcriptionList);
        this.transcriptionList.forEach(el -> el.setRecord(this.record));
        this.transcriptionRepository.saveAll(this.transcriptionList);
        this.record = this.recordsRepository.save(this.record);
    }

    @Before
    public void init() {
        this.prepareEasMock();
        this.prepareTpsMock();
        this.initTranscriptions();
        this.initRecord();
        doNothing().when(ens).notifyPublishers(any());
    }

    @Test
    @Rollback
    @Transactional
    public void willUpdateFromRestoreConstructor() {
        UpdateTask updateTask = new UpdateTask(this.record, this.qrs, this.tps, this.ess, this.eas, this.contextMediator);
        updateTask.process();
        assertEquals(
                Record.RecordState.NORMAL,
                this.recordsRepository.findByIdentifier(this.RECORD_IDENTIFIER).get().getState()
        );
    }

    @SneakyThrows
    @Test
    @Rollback
    @Transactional
    public void willUpdateFromNormalConstructor() {
        UpdateTask updateTask = new UpdateTask(this.RECORD_IDENTIFIER, this.ANNOTATION_ID, this.TP_ID, this.qrs, this.tps, this.ess, this.eas, this.contextMediator);
        updateTask.process();
        assertEquals(
                Record.RecordState.NORMAL,
                this.recordsRepository.findByIdentifier(this.RECORD_IDENTIFIER).get().getState()
        );
    }


    @SneakyThrows
    @Test
    @Rollback
    @Transactional
    public void areUpdatesIdempotent() {
        UpdateTask updateTask = new UpdateTask(this.RECORD_IDENTIFIER, this.ANNOTATION_ID, this.TP_ID, this.qrs, this.tps, this.ess, this.eas, this.contextMediator);
        updateTask.process();
        updateTask.process();
        updateTask.process();
        updateTask.process();
        updateTask.process();
        assertEquals(
                Record.RecordState.NORMAL,
                this.recordsRepository.findByIdentifier(this.RECORD_IDENTIFIER).get().getState()
        );
        // two as one annotation is forced to be created in @Before for earlier test and second in this test
        assertEquals(
                2,
                this.transcriptionRepository.findAllByTpId(this.TP_ID).size()
        );
    }
}
