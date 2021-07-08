package pl.psnc.dei.service.context;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.DAO.ConversionContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionContext;

import java.util.Optional;

@Service
public class ConversionContextService extends ContextService<ConversionContext> {

    private final ConversionContextRepository conversionContextRepository;

    public ConversionContextService(ConversionContextRepository conversionContextRepository) {
        this.conversionContextRepository = conversionContextRepository;
    }

    @Override
    public ConversionContext get(Record record) {
        Optional<ConversionContext> context = this.conversionContextRepository.findByRecord(record);
        return context.orElseGet(() -> ConversionContext.from(record));
    }

    @Override
    public ConversionContext save(ConversionContext context) {
        return null;
    }
}
