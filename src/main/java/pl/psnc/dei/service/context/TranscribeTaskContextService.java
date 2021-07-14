package pl.psnc.dei.service.context;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.DAO.TranscribeTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;

import java.util.Arrays;
import java.util.Optional;



@Service
public class TranscribeTaskContextService extends ContextService<TranscribeTaskContext> {

    private final TranscribeTaskContextRepository transcribeTaskContextRepository;

    public TranscribeTaskContextService(TranscribeTaskContextRepository transcribeTaskContextRepository) {
        this.transcribeTaskContextRepository = transcribeTaskContextRepository;
    }

    @Override
    public TranscribeTaskContext get(Record record) {
        Optional<TranscribeTaskContext> context = this.transcribeTaskContextRepository.findByRecord(record);
        return context.orElseGet(() -> TranscribeTaskContext.from(record));
    }

    @Override
    public TranscribeTaskContext save(TranscribeTaskContext context) {
        return this.transcribeTaskContextRepository.save(context);
    }

    @Override
    public void delete(TranscribeTaskContext context) {
        this.transcribeTaskContextRepository.delete(context);
    }

    @Override
    public Boolean canHandle(Record record) {
        return Arrays.asList(Record.RecordState.T_PENDING, Record.RecordState.T_SENT).contains(record.getState());
    }

    @Override
    public Boolean canHandle(Class<?> aClass) {
        return aClass.isAssignableFrom(TranscribeTaskContext.class);
    }
}
