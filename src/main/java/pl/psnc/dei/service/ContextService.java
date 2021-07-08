package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.ConversionContextRepository;
import pl.psnc.dei.model.DAO.ConversionTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;
import pl.psnc.dei.model.conversion.ConversionContext;
import pl.psnc.dei.model.conversion.ConversionTaskContext;
import pl.psnc.dei.queue.task.ConversionTask;
import pl.psnc.dei.queue.task.Task;

import java.util.Optional;

/**
 * Service used to manage context of processing. Each context could be possibly saved in DB by usage of this class
 */
@Service
public class ContextService {

    private final ConversionTaskContextRepository conversionTaskContextRepository;

    private final ConversionContextRepository conversionContextRepository;

    public ContextService(ConversionTaskContextRepository conversionTaskContextRepository, ConversionContextRepository conversionContextRepository){
        this.conversionTaskContextRepository = conversionTaskContextRepository;
        this.conversionContextRepository = conversionContextRepository;
    };

    public Context getTaskContext(Task task) {
        if (task.getRecord().getState() == Record.RecordState.C_PENDING) {
            return this.getConversionTaskContext((ConversionTask) task);
        }
        else throw new IllegalArgumentException(task.getRecord().getState().name());
    }

    public <T extends Context> T getRecordContext(Record record, Class<T> contextClass) {
        if (contextClass.isAssignableFrom(ConversionContext.class)) {
            return (T) this.getConversionContext(record);
        }
        else {
            throw new IllegalArgumentException(record.getState().name());
        }
    }

    private ConversionTaskContext getConversionTaskContext(ConversionTask task) {
        Optional<ConversionTaskContext> context = this.conversionTaskContextRepository.findAllByRecord(task.getRecord());
        return context.orElseGet(() -> ConversionTaskContext.from(task));
    }

    private ConversionContext getConversionContext(Record record) {
        Optional<ConversionContext> context = this.conversionContextRepository.findByRecord(record);
        return context.orElseGet(() -> ConversionContext.from(record));
    }
}
