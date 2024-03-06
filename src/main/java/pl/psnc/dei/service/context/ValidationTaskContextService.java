package pl.psnc.dei.service.context;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.ValidationTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ValidationTaskContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;


@Service
public class ValidationTaskContextService implements ContextService<ValidationTaskContext> {

    private final ValidationTaskContextRepository validationTaskContextRepository;

    private final Collection<Record.RecordState> handleable = Arrays.asList(Record.RecordState.V_PENDING);

    public ValidationTaskContextService(ValidationTaskContextRepository validationTaskContextRepository) {
        this.validationTaskContextRepository = validationTaskContextRepository;
    }

    @Override
    @Transactional
    public ValidationTaskContext get(Record record) {
        Optional<ValidationTaskContext> context = this.validationTaskContextRepository.findByRecord(record);
        if(context.isPresent()) {
            ValidationTaskContext validationTaskContext = context.get();
            validationTaskContext.getExceptions().forEach(Hibernate::initialize);
        }
        return context.orElseGet(() -> ValidationTaskContext.from(record));
    }

    @Override
    public ValidationTaskContext save(ValidationTaskContext context) {
        return this.validationTaskContextRepository.save(context);
    }

    @Override
    public void delete(ValidationTaskContext context) {
        this.validationTaskContextRepository.delete(context);
    }

    @Override
    public boolean canHandle(Record record) {
        return this.handleable.contains(record.getState());
    }

    @Override
    public boolean canHandle(Class<?> aClass) {
        return aClass.isAssignableFrom(ValidationTaskContext.class);
    }
}
