package pl.psnc.dei.controllers;

import lombok.SneakyThrows;
import org.apache.jena.atlas.json.JSON;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.exception.TranscriptionDuplicationException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.DAO.TranscriptionRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.QueueRecordService;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@ActiveProfiles("integration")
@SpringBootTest
@AutoConfigureMockMvc
public class TranscriptionControllerTest {
    // if TranscriptionService will be implemented this tests should be moved to service specific class

    private final String RECORD_IDENTIFIER = "sample-record-identifier";
    @Autowired
    private RecordsRepository recordsRepository;
    @Autowired
    private TranscriptionRepository transcriptionRepository;
    @Autowired
    private QueueRecordService qrs;
    private Transcription sampleTranscription;
    private Record sampleRecord;

    @Before
    public void initTranscription() {
        this.sampleRecord = new Record();
        this.sampleRecord.setTitle("Some title as test");
        this.sampleRecord.setAggregator(Aggregator.EUROPEANA);
        this.sampleRecord.setState(Record.RecordState.NORMAL);
        this.sampleRecord.setIdentifier(this.RECORD_IDENTIFIER);
        this.sampleRecord.setTranscriptions(new ArrayList<>());
        this.sampleRecord = this.recordsRepository.save(this.sampleRecord);

        this.sampleTranscription = new Transcription();
        this.sampleTranscription.setTpId("34rfwe");
        this.sampleTranscription.setAnnotationId("432rdwfd");
        this.sampleTranscription.setTranscriptionContent(JSON.parse("{\"a\":\"b\"}"));
        this.sampleTranscription.setRecord(this.sampleRecord);
        this.sampleRecord.getTranscriptions().add(this.sampleTranscription);

        this.sampleTranscription = this.transcriptionRepository.save(this.sampleTranscription);
        this.sampleRecord = this.recordsRepository.save(this.sampleRecord);
    }

    @SneakyThrows
    @Ignore
    @Test(expected = TranscriptionDuplicationException.class)
    @Transactional
    @Rollback
    public void ifTranscriptionDuplicated_thenThrowException() {
        this.qrs.throwIfTranscriptionExistFor(this.RECORD_IDENTIFIER);
    }
}
