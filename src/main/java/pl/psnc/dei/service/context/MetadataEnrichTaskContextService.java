package pl.psnc.dei.service.context;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.MetadataEnrichTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.MetadataEnrichTaskContext;

import java.util.Optional;

@Service
public class MetadataEnrichTaskContextService implements ContextService<MetadataEnrichTaskContext> {

    private final MetadataEnrichTaskContextRepository metadataEnrichTaskContextRepository;

    public MetadataEnrichTaskContextService(MetadataEnrichTaskContextRepository metadataEnrichTaskContextRepository) {
        this.metadataEnrichTaskContextRepository = metadataEnrichTaskContextRepository;
    }

    @Override
    @Transactional
    public MetadataEnrichTaskContext get(Record record) {
        Optional<MetadataEnrichTaskContext> context = this.metadataEnrichTaskContextRepository.findByRecord(record);
        // initialization needed cuz further use of this collection will happen outside transaction,
        // and there is no way to annotate method / class as Transactional
        if (context.isPresent()) {
            MetadataEnrichTaskContext metadataEnrichTaskContext = context.get();
            Hibernate.initialize(metadataEnrichTaskContext.getSavedEnrichments());
        }
        return context.orElseGet(() -> MetadataEnrichTaskContext.from(record));
    }

    @Override
    public MetadataEnrichTaskContext save(MetadataEnrichTaskContext context) {
        return this.metadataEnrichTaskContextRepository.save(context);
    }

    @Override
    public void delete(MetadataEnrichTaskContext context) {
        this.metadataEnrichTaskContextRepository.delete(context);
    }

    @Override
    public boolean canHandle(Record record) {
        return record.getState().equals(Record.RecordState.M_PENDING) || record.getState().equals(Record.RecordState.ME_PENDING);
    }

    @Override
    public boolean canHandle(Class<?> aClass) {
        return aClass.isAssignableFrom(MetadataEnrichTaskContext.class);
    }
}
