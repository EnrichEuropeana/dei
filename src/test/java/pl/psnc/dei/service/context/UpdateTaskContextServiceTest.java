package pl.psnc.dei.service.context;

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
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.DAO.UpdateTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.UpdateTaskContext;
import pl.psnc.dei.service.QueueRecordService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
@Transactional
public class UpdateTaskContextServiceTest {
    private final String RECORD_IDENTIFIER = "test_identifier";

    @Autowired
    private UpdateTaskContextService updateTaskContextService;

    @Autowired
    private QueueRecordService queueRecordService;

    @Autowired
    private UpdateTaskContextRepository updateTaskContextRepository;

    @Autowired
    private RecordsRepository recordsRepository;

    private Record record;
    private UpdateTaskContext context;

    @SneakyThrows
    @Before
    public void initRecord() {
        this.record = new Record();
        this.record.setTitle("Just testing");
        this.record.setIdentifier(this.RECORD_IDENTIFIER);
        this.record.setState(Record.RecordState.U_PENDING);
        this.queueRecordService.saveRecord(this.record);
        this.record = this.queueRecordService.getRecord(this.RECORD_IDENTIFIER);
    }

    @Before
    public void initContext() {
        this.context = new UpdateTaskContext();
        this.context.setRecord(this.record);
        this.context.setHasFetchedUpdatedTranscriptions(true);
    }

    @Test
    @Rollback
    public void writeAndReadToDB() {
        this.updateTaskContextService.save(this.context);
        UpdateTaskContext fetchedContext = this.updateTaskContextService.get(this.record);
        assertEquals(this.context.getRecord().getId(), fetchedContext.getRecord().getId());
        assertEquals(this.context.getTaskState(), fetchedContext.getTaskState());
    }

    @Test
    @Rollback
    public void deleteFromDB() {
        this.updateTaskContextService.save(this.context);

        // check if saved
        // use repository for lower level access
        Optional<UpdateTaskContext> fetchedContext = this.updateTaskContextRepository.findByRecord(this.record);
        assertTrue(fetchedContext.isPresent());

        this.updateTaskContextService.delete(this.context);
        fetchedContext = this.updateTaskContextRepository.findByRecord(this.record);
        assertTrue(fetchedContext.isEmpty());
    }

    @Test
    @Rollback
    public void canHandleWithClassArgument() {
        assertTrue(
                this.updateTaskContextService.canHandle(UpdateTaskContext.class)
        );
    }

    @Test
    @Rollback
    public void canHandleWithRecordArgument() {
        assertTrue(
                this.updateTaskContextService.canHandle(this.record)
        );
    }
}
