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

import java.util.List;
import java.util.Optional;

/**
 * Mediator used to save and fetch context for tasks
 */
@SuppressWarnings("rawtypes")
@Service
public class ContextMediator {

    private final List<ContextService> contextServiceList;

    public ContextMediator(List<ContextService> contextServiceList){
        this.contextServiceList = contextServiceList;
    };

    public Context get(Record record) {
        for (ContextService contextService : this.contextServiceList) {
            if (contextService.canHandle(record)) {
                return (Context) contextService.get(record);
            }
        }
        throw new IllegalArgumentException(record.getState().toString());
    }

    public Context save(Context context) {
        for (ContextService contextService : this.contextServiceList) {
            if (contextService.canHandle(context.getRecord())) {
                return (Context) contextService.save(context);
            }
        }
        throw new IllegalArgumentException("Cannot save record in state: " + context.getRecord().getState().toString());
    }

    public void delete(Context context) {
        for (ContextService contextService : this.contextServiceList) {
            if (contextService.canHandle(context.getRecord())) {
                contextService.delete(context);
            }
        }
    }

}
