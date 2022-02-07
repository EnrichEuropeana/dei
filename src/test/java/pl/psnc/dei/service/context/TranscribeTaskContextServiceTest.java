package pl.psnc.dei.service.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
@Transactional
public class TranscribeTaskContextServiceTest {
    private final String RECORD_IDENTIFER = "test-record";
    @Autowired
    private TranscribeTaskContextService transcribeTaskContextService;

    @Autowired
    private RecordsRepository recordsRepository;

    private TranscribeTaskContext context;
    private Record record;

    @Before
    public void initRecord() {
        this.record = new Record();
        this.record.setIdentifier(this.RECORD_IDENTIFER);
        this.record.setState(Record.RecordState.T_PENDING);
        this.record.setTitle("test");
        this.record = this.recordsRepository.save(this.record);
    }

    @Before
    public void initContext() {
        this.context = new TranscribeTaskContext();
        this.context.setRecord(this.record);
        this.context.setHasAddedFailure(true);
        this.context.setExceptions(new ArrayList<>());
    }

    @Test
    @Rollback
    public void readAndWriteToDB() {
        this.transcribeTaskContextService.save(this.context);
        TranscribeTaskContext fetched = this.transcribeTaskContextService.get(this.record);
        assertEquals(fetched.getRecord().getId(), this.record.getId());
    }

    @Test
    @Rollback
    public void deleteFromDB() {
        this.transcribeTaskContextService.save(this.context);
        TranscribeTaskContext fetched = this.transcribeTaskContextService.get(this.record);

        assertNotNull(fetched);

        this.transcribeTaskContextService.delete(this.context);
        fetched = this.transcribeTaskContextService.get(this.record);
        assertNull(fetched.getId());
    }

    @Test
    @Rollback
    public void canHandleWithClassArgument() {
        assertTrue(
                this.transcribeTaskContextService.canHandle(TranscribeTaskContext.class)
        );
    }

    @Test
    @Rollback
    public void canHandleWithRecordArgument() {
        assertTrue(
                this.transcribeTaskContextService.canHandle(this.record)
        );
    }
}
