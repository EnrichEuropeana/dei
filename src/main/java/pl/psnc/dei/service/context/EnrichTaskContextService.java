package pl.psnc.dei.service.context;

import pl.psnc.dei.model.DAO.EnrichTaskContextRepository;
import pl.psnc.dei.model.DAO.TranscribeTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.EnrichTaskContext;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;

import java.util.Optional;

public class EnrichTaskContextService extends ContextService<EnrichTaskContext> {

    private final EnrichTaskContextRepository enrichTaskContextRepository;

    public EnrichTaskContextService(EnrichTaskContextRepository enrichTaskContextRepository) {
        this.enrichTaskContextRepository = enrichTaskContextRepository;
    }

    @Override
    public EnrichTaskContext get(Record record) {
        Optional<EnrichTaskContext> context = this.enrichTaskContextRepository.findByRecord(record);
        return context.orElseGet(() -> EnrichTaskContext.from(record));
    }

    @Override
    public EnrichTaskContext save(EnrichTaskContext context) {
        return null;
    }

    @Override
    public Boolean canHandle(Record record) {
        return record.getState().equals(Record.RecordState.E_PENDING);
    }
}
