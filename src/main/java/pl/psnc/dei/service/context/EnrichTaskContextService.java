package pl.psnc.dei.service.context;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.EnrichTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.EnrichTaskContext;

import java.util.Optional;

@Service
public class EnrichTaskContextService implements ContextService<EnrichTaskContext> {

    private final EnrichTaskContextRepository enrichTaskContextRepository;

    public EnrichTaskContextService(EnrichTaskContextRepository enrichTaskContextRepository) {
        this.enrichTaskContextRepository = enrichTaskContextRepository;
    }

    @Override
    @Transactional
    public EnrichTaskContext get(Record record) {
        Optional<EnrichTaskContext> context = this.enrichTaskContextRepository.findByRecord(record);
        // initialization needed cuz further use of this collection will happen outside transaction,
        // and there is no way to annotate method / class as Transactional
        if (context.isPresent()) {
            EnrichTaskContext enrichTaskContext = context.get();
            Hibernate.initialize(enrichTaskContext.getSavedTranscriptions());
        }
        return context.orElseGet(() -> EnrichTaskContext.from(record));
    }

    @Override
    public EnrichTaskContext save(EnrichTaskContext context) {
        return this.enrichTaskContextRepository.save(context);
    }

    @Override
    public void delete(EnrichTaskContext context) {
        this.enrichTaskContextRepository.delete(context);
    }

    @Override
    public Boolean canHandle(Record record) {
        return record.getState().equals(Record.RecordState.E_PENDING);
    }

    @Override
    public Boolean canHandle(Class<?> aClass) {
        return aClass.isAssignableFrom(EnrichTaskContext.class);
    }
}
