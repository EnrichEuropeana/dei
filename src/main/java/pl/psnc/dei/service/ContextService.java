package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.ConversionContextRepository;
import pl.psnc.dei.model.DAO.ConversionTaskContextRepository;
import pl.psnc.dei.model.DAO.TranscribeTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;
import pl.psnc.dei.model.conversion.ConversionContext;
import pl.psnc.dei.model.conversion.ConversionTaskContext;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;
import pl.psnc.dei.queue.task.ConversionTask;
import pl.psnc.dei.queue.task.Task;
import pl.psnc.dei.queue.task.TranscribeTask;

import java.util.Optional;

/**
 * Service used to manage context of processing. Each context could be possibly saved in DB by usage of this class
 */
@Service
public class ContextService {

    private final ConversionTaskContextRepository conversionTaskContextRepository;

    private final ConversionContextRepository conversionContextRepository;

    private final TranscribeTaskContextRepository transcribeTaskContextRepository;

    public ContextService(ConversionTaskContextRepository conversionTaskContextRepository, ConversionContextRepository conversionContextRepository, TranscribeTaskContextRepository transcribeTaskContextRepository){
        this.conversionTaskContextRepository = conversionTaskContextRepository;
        this.conversionContextRepository = conversionContextRepository;
        this.transcribeTaskContextRepository = transcribeTaskContextRepository;
    };

    /**
     * Used to obtain context of task if applicable
     * @param task task for which context will be obtained
     * @return context
     */
    public Context getTaskContext(Task task) {
        if (task.getRecord().getState() == Record.RecordState.C_PENDING) {
            return this.getConversionTaskContext((ConversionTask) task);
        }
        else if (task.getRecord().getState() == Record.RecordState.T_PENDING) {
            return this.getTranscribeTaskContext((TranscribeTask) task);
        }
        else throw new IllegalArgumentException(task.getRecord().getState().name());
    }

    /**
     * In some cases context must be persisted by
     * @param record
     * @param contextClass
     * @param <T>
     * @return
     */
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

    private TranscribeTaskContext getTranscribeTaskContext(TranscribeTask task) {
        Optional<TranscribeTaskContext> context = this.transcribeTaskContextRepository.findByRecord(task.getRecord());
        return context.orElseGet(() -> TranscribeTaskContext.from(task.getRecord()));
    }
}