package pl.psnc.dei.service.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ContextMediatorTest {
    @Mock
    private ConversionTaskContextService conversionTaskContextService;

    @Mock
    private EnrichTaskContextService enrichTaskContextService;

    @Mock
    private TranscribeTaskContextService transcribeTaskContextService;

    @Mock
    private UpdateTaskContextService updateTaskContextService;

    private ContextMediator contextMediator;

    private Record conversionStateRecord;
    private Record updateStateRecord;
    private Record enrichStateRecord;
    private Record transcribeStateRecord;

    @Before
    public void initRecords() {
        this.conversionStateRecord = new Record();
        this.conversionStateRecord.setState(Record.RecordState.C_PENDING);

        this.updateStateRecord = new Record();
        this.updateStateRecord.setState(Record.RecordState.U_PENDING);

        this.enrichStateRecord = new Record();
        this.enrichStateRecord.setState(Record.RecordState.E_PENDING);

        this.transcribeStateRecord = new Record();
        this.transcribeStateRecord.setState(Record.RecordState.T_PENDING);
    }

    private void mockConversionTaskContextService() {
        when(this.conversionTaskContextService.get(any()))
                .thenReturn(new ConversionTaskContext());
        when(this.conversionTaskContextService.canHandle(ConversionTaskContext.class))
                .thenReturn(true);
        when(this.conversionTaskContextService.canHandle(this.conversionStateRecord))
                .thenReturn(true);
    }

    private void mockEnrichTaskContextService() {
        when(this.enrichTaskContextService.get(any()))
                .thenReturn(new EnrichTaskContext());
        when(this.enrichTaskContextService.canHandle(EnrichTaskContext.class))
                .thenReturn(true);
        when(this.enrichTaskContextService.canHandle(this.enrichStateRecord))
                .thenReturn(true);
    }

    private void mockTranscribeTaskContextService() {
        when(this.transcribeTaskContextService.get(any()))
                .thenReturn(new TranscribeTaskContext());
        when(this.transcribeTaskContextService.canHandle(TranscribeTaskContext.class))
                .thenReturn(true);
        when(this.transcribeTaskContextService.canHandle(this.transcribeStateRecord))
                .thenReturn(true);
    }

    private void mockUpdateTaskContextService() {
        when(this.updateTaskContextService.get(any()))
                .thenReturn(new UpdateTaskContext());
        when(this.updateTaskContextService.canHandle(UpdateTaskContext.class))
                .thenReturn(true);
        when(this.updateTaskContextService.canHandle(this.updateStateRecord))
                .thenReturn(true);
    }

    private void createMocks() {
        this.mockConversionTaskContextService();
        this.mockTranscribeTaskContextService();
        this.mockEnrichTaskContextService();
        this.mockUpdateTaskContextService();

        List<ContextService> services = List.of(
                this.conversionTaskContextService,
                this.updateTaskContextService,
                this.enrichTaskContextService,
                this.transcribeTaskContextService
        );
        this.contextMediator = new ContextMediator(services);
    }

    @Test
    public void whenRecordInConvertState_thenReturnConvertContext() {
        this.createMocks();
        Context context = this.contextMediator.get(this.conversionStateRecord);
        assertEquals(context.getClass(), ConversionTaskContext.class);
    }

    @Test
    public void whenRecordInUpdateState_thenReturnUpdateContext() {
        this.createMocks();
        Context context = this.contextMediator.get(this.updateStateRecord);
        assertEquals(context.getClass(), UpdateTaskContext.class);
    }

    @Test
    public void whenRecordInEnrichState_thenReturnEnrichContext() {
        this.createMocks();
        Context context = this.contextMediator.get(this.enrichStateRecord);
        assertEquals(context.getClass(), EnrichTaskContext.class);
    }

    @Test
    public void whenRecordInTrasncribeState_thenReturnTranscribeContext() {
        this.createMocks();
        Context context = this.contextMediator.get(this.transcribeStateRecord);
        assertEquals(context.getClass(), TranscribeTaskContext.class);
    }
}
