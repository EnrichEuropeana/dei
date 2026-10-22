package pl.psnc.dei.service.context;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;

import java.util.List;

/**
 * Mediator used to save and fetch context for tasks
 */
@SuppressWarnings("rawtypes")
@Service
public class ContextMediator {

    private final List<ContextService> contextServiceList;

    public ContextMediator(List<ContextService> contextServiceList){
        this.contextServiceList = contextServiceList;
    }

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

    // one can force mediator to use defined context service
    public <T> void delete(Context context, Class<T> contextClass) {
        for (ContextService contextService : this.contextServiceList) {
            if (contextService.canHandle(contextClass)) {
                contextService.delete(context);
            }
        }
    }

    public <T> Context get(Record record, Class<T> contextClass) {
        for (ContextService contextService : this.contextServiceList) {
            if (contextService.canHandle(contextClass)) {
                return (Context) contextService.get(record);
            }
        }
        throw new IllegalArgumentException("Cannot fetch record for task class: " + contextClass.toString());
    }

}
