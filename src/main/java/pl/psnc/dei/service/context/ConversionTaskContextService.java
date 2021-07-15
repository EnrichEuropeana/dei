package pl.psnc.dei.service.context;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.ConversionTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.util.Optional;

@Service
public class ConversionTaskContextService extends ContextService<ConversionTaskContext> {

    private final ConversionTaskContextRepository conversionTaskContextRepository;

    public ConversionTaskContextService(ConversionTaskContextRepository conversionTaskContextRepository) {
                this.conversionTaskContextRepository = conversionTaskContextRepository;
            }

    @Override
    @Transactional
    public ConversionTaskContext get(Record record) {
        Optional<ConversionTaskContext> context = this.conversionTaskContextRepository.findAllByRecord(record);
        if (context.isPresent()) {
            ConversionTaskContext conversionTaskContext = context.get();
            // initialization needed cuz further use of this collection will happen outside transaction,
            // and there is no way to annotate method / class as Transactional
            conversionTaskContext.getRawConversionData()
                    .forEach(el -> {
                        el.getOutFilePath().forEach(Hibernate::initialize);
                        el.getImagePath().forEach(Hibernate::initialize);
                        el.getDimension().forEach(Hibernate::initialize);
                    });
            Hibernate.initialize(conversionTaskContext.getExceptions());
            return conversionTaskContext;
        }
        return ConversionTaskContext.from(record);
    }

    @Override
    public ConversionTaskContext save(ConversionTaskContext context) {
        return this.conversionTaskContextRepository.save(context);
    }

    @Override
    public void delete(ConversionTaskContext context) {
        this.conversionTaskContextRepository.delete(context);
    }

    @Override
    public Boolean canHandle(Record record) {
        return record.getState().equals(Record.RecordState.C_PENDING);
    }

    @Override
    public Boolean canHandle(Class<?> aClass) {
        return aClass.isAssignableFrom(ConversionTaskContext.class);
    }
}
