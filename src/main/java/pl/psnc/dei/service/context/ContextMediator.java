package pl.psnc.dei.service.context;

import org.hibernate.sql.Update;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.*;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.*;
import pl.psnc.dei.queue.task.ConversionTask;
import pl.psnc.dei.queue.task.Task;
import pl.psnc.dei.queue.task.TranscribeTask;

import java.util.Optional;

/**
 * Service used to manage context of processing. Each context could be possibly saved in DB by usage of this class
 */
@Service
public class ContextMediator {



    private final TranscribeTaskContextRepository transcribeTaskContextRepository;
    private final EnrichTaskContextRepository enrichTaskContextRepository;

    public ContextMediator(ConversionTaskContextRepository conversionTaskContextRepository, ConversionContextRepository conversionContextRepository, TranscribeTaskContextRepository transcribeTaskContextRepository, EnrichTaskContextRepository enrichTaskContextRepository, UpdateTaskContextRepository updateTaskContextRepository){



        this.transcribeTaskContextRepository = transcribeTaskContextRepository;
        this.enrichTaskContextRepository = enrichTaskContextRepository;
    };

    /**
     * Used to obtain context of task if applicable
     * @param task task for which context will be obtained
     * @return context
     */
    public Context getTaskContext(Task task) {
        if (task.getRecord().getState() == Record.RecordState.C_PENDING) {
            return this.getConversionTaskContext( task);
        }
        else if (task.getRecord().getState() == Record.RecordState.T_PENDING) {
            return this.getTranscribeTaskContext(task);
        }
        else if (task.getRecord().getState() == Record.RecordState.E_PENDING) {
            return this.getEnrichTaskContext(task);
        }
        else if (task.getRecord().getState() == Record.RecordState.U_PENDING) {
            return this.getUpdateTaskContext(task);
        }
        else throw new IllegalArgumentException(task.getRecord().getState().name());
    }

    /**
     * In some cases context must be persisted by
     * @param record record for which context should be fetched
     * @param contextClass class of context to fetch
     * @return context
     */
    public <T extends Context> T getRecordContext(Record record, Class<T> contextClass) {
        if (contextClass.isAssignableFrom(ConversionContext.class)) {
            return (T) this.getConversionContext(record);
        }
        else {
            throw new IllegalArgumentException(record.getState().name());
        }
    }

    public <T extends Context> T saveContext(Task task) {

    }

    private ConversionTaskContext getConversionTaskContext(Task task) {

    }

    private ConversionContext getConversionContext(Record record) {

    }

    private TranscribeTaskContext getTranscribeTaskContext(Task task) {
        Optional<TranscribeTaskContext> context = this.transcribeTaskContextRepository.findByRecord(task.getRecord());
        return context.orElseGet(() -> TranscribeTaskContext.from(task.getRecord()));
    }

    private EnrichTaskContext getEnrichTaskContext(Task task) {
        Optional<EnrichTaskContext> context = this.enrichTaskContextRepository.findByRecord(task.getRecord());
        return context.orElseGet(() -> EnrichTaskContext.from(task.getRecord()));
    }
}
