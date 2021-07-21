package pl.psnc.dei.service.context;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.TranscribeTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;


@Service
public class TranscribeTaskContextService implements ContextService<TranscribeTaskContext> {

    private final TranscribeTaskContextRepository transcribeTaskContextRepository;

    private final Collection<Record.RecordState> handleable = Arrays.asList(Record.RecordState.T_PENDING, Record.RecordState.T_SENT);

    public TranscribeTaskContextService(TranscribeTaskContextRepository transcribeTaskContextRepository) {
        this.transcribeTaskContextRepository = transcribeTaskContextRepository;
    }

    @Override
    @Transactional
    public TranscribeTaskContext get(Record record) {
        Optional<TranscribeTaskContext> context = this.transcribeTaskContextRepository.findByRecord(record);
        if(context.isPresent()) {
            TranscribeTaskContext transcribeTaskContext = context.get();
            transcribeTaskContext.getExceptions().forEach(Hibernate::initialize);
        }
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
        return this.handleable.contains(record.getState());
    }

    @Override
    public Boolean canHandle(Class<?> aClass) {
        return aClass.isAssignableFrom(TranscribeTaskContext.class);
    }
}
