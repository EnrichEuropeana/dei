package pl.psnc.dei.service.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.EnrichTaskContext;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
@Transactional
public class EnrichTaskContextServiceTest {
    private final String RECORD_IDENTIFIER = "test-record";

    @Autowired
    private EnrichTaskContextService enrichTaskContextService;

    @Autowired
    private RecordsRepository recordsRepository;

    private EnrichTaskContext context;
    private Record record;

    @Before
    public void initRecord() {
        this.record = new Record();
        this.record.setState(Record.RecordState.E_PENDING);
        this.record.setIdentifier(this.RECORD_IDENTIFIER);
        this.record = this.recordsRepository.save(this.record);
    }

    @Before
    public void initEnrichContext() {
        this.context = new EnrichTaskContext();
        this.context.setRecord(this.record);
        this.context.setHasDownloadedEnrichment(true);
    }

    @Test
    @Rollback
    public void readAndWriteDB() {
        this.enrichTaskContextService.save(this.context);
        EnrichTaskContext fetched = this.enrichTaskContextService.get(this.record);
        assertEquals(fetched.getRecord().getId(), this.record.getId());
        assertEquals(fetched.getTaskState(), fetched.getTaskState());
    }

    @Test
    @Rollback
    public void deleteFromDB() {
        this.enrichTaskContextService.save(this.context);

        EnrichTaskContext fetched = this.enrichTaskContextService.get(this.record);
        assertNotNull(fetched);

        this.enrichTaskContextService.delete(this.context);
        fetched = this.enrichTaskContextService.get(this.record);
        // service will try to initialize new context if no was found in DB, thus we are not checkind
        // for null on entire object but rather id
        assertNull(fetched.getId());
    }

    @Test
    @Rollback
    public void canHandleWithClassArgument() {
        assertTrue(
                this.enrichTaskContextService.canHandle(EnrichTaskContext.class)
        );
    }

    @Test
    @Rollback
    public void canHandleWithRecordArgument() {
        assertTrue(
                this.enrichTaskContextService.canHandle(this.record)
        );
    }
}
