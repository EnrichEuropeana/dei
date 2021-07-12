package pl.psnc.dei.service.context;

import org.springframework.stereotype.Service;
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
    public ConversionTaskContext get(Record record) {
        Optional<ConversionTaskContext> context = this.conversionTaskContextRepository.findAllByRecord(record);
        return context.orElseGet(() -> ConversionTaskContext.from(record));
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
}
