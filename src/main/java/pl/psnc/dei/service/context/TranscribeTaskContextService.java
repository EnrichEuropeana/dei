package pl.psnc.dei.service.context;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.DAO.TranscribeTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;

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
        return null;
    }

    @Override
    public Boolean canHandle(Record record) {
        return record.getState().equals(Record.RecordState.T_PENDING);
    }
}
