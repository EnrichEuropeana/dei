package pl.psnc.dei.queue.task;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.DAO.TranscriptionRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
public class TaskFactoryTest {

    private final String ANNOTATION_ID = "53612";
    private final String TP_ID = "203544";
    // https://europeana.transcribathon.eu/documents/story/item/?item=1180085
    // JOHN (NICKNAMED JACK) HENRY MALLETT'S TRAGIC DEATH
    private final String U_PENDING_RECORD_IDENTIFIER = "/2020601/https___1914_1918_europeana_eu_contributions_17173";
    // https://europeana.transcribathon.eu/documents/story/item/?item=1160812
    // MARIA VON STUTTERHEIM DOKUMENTIERT DEN KRIEG
    private final String C_PENDING_RECORD_IDENTIFIER = "/2020601/https___1914_1918_europeana_eu_contributions_12746";

    @Autowired
    private TasksFactory tasksFactory;

    private Record T_PENDINGRecord;
    private Record E_PENDINGRecord;
    private Record U_PENDINGRecord;
    private Record C_PENDINGRecord;
    private Record T_SENTRecord;

    @Autowired
    private TranscriptionRepository transcriptionRepository;

    @Autowired
    private RecordsRepository recordsRepository;

    @Before
    public void prepareRecords() {
        this.T_PENDINGRecord = new Record();
        this.T_PENDINGRecord.setState(Record.RecordState.T_PENDING);

        this.E_PENDINGRecord = new Record();
        this.E_PENDINGRecord.setState(Record.RecordState.E_PENDING);

        this.U_PENDINGRecord = new Record();
        this.U_PENDINGRecord.setIdentifier(this.U_PENDING_RECORD_IDENTIFIER);
        this.U_PENDINGRecord.setTranscriptions(new ArrayList<>());
        this.U_PENDINGRecord.setState(Record.RecordState.U_PENDING);
        this.U_PENDINGRecord = this.recordsRepository.save(this.U_PENDINGRecord);
        Transcription transcription = new Transcription();
        transcription.setAnnotationId(this.ANNOTATION_ID);
        transcription.setTpId(this.TP_ID);
        transcription.setRecord(this.U_PENDINGRecord);
        this.transcriptionRepository.save(transcription);
        this.U_PENDINGRecord.getTranscriptions().add(transcription);
        this.U_PENDINGRecord = this.recordsRepository.save(U_PENDINGRecord);

        this.C_PENDINGRecord = new Record();
        this.C_PENDINGRecord.setIdentifier(this.C_PENDING_RECORD_IDENTIFIER);
        this.C_PENDINGRecord.setAggregator(Aggregator.EUROPEANA);
        this.C_PENDINGRecord.setState(Record.RecordState.C_PENDING);

        this.T_SENTRecord = new Record();
        this.T_SENTRecord.setState(Record.RecordState.T_SENT);
    }

    @Test
    @Rollback
    @Transactional
    public void canResolveT_PENDING() {
        assertEquals(
                TranscribeTask.class,
                this.tasksFactory.getTask(this.T_PENDINGRecord).getClass()
        );
    }

    @Test
    @Rollback
    @Transactional
    public void canResolveE_PENDING() {
        assertEquals(
                EnrichTask.class,
                this.tasksFactory.getTask(this.E_PENDINGRecord).getClass()
        );
    }

    @Test
    @Rollback
    @Transactional
    public void canResolveU_PENDING() {
        assertEquals(
                UpdateTask.class,
                this.tasksFactory.getTask(this.U_PENDINGRecord).getClass()
        );
    }

    @Test
    @Rollback
    @Transactional
    public void canResolveC_PENGIN() {
        assertEquals(
                ConversionTask.class,
                this.tasksFactory.getTask(this.C_PENDINGRecord).getClass()
        );
    }

    @Test(expected = RuntimeException.class)
    @Rollback
    @Transactional
    public void willNotResolveT_SENT() {
        this.tasksFactory.getTask(this.T_SENTRecord);
    }

    @SneakyThrows
    @Test
    @Rollback
    @Transactional
    public void willCreateUpdateTaskFromRecordId() {
        assertEquals(
                UpdateTask.class,
                this.tasksFactory.getNewUpdateTask(this.U_PENDINGRecord.getIdentifier(), this.ANNOTATION_ID, this.TP_ID).getClass()
        );
    }
}
