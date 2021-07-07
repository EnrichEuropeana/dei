package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.ConversionTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;
import pl.psnc.dei.model.conversion.ConversionTaskContext;
import pl.psnc.dei.queue.task.ConversionTask;
import pl.psnc.dei.queue.task.Task;

import java.util.Optional;

@Service
public class ContextService {

    private ConversionTaskContextRepository conversionTaskContextRepository;

    public ContextService(ConversionTaskContextRepository conversionTaskContextRepository){
        this.conversionTaskContextRepository = conversionTaskContextRepository;
    };

    public Context getContext(Task task) {
        if (task.getRecord().getState() == Record.RecordState.C_PENDING) {
            return this.getConversionTaskContext((ConversionTask) task);
        }
        else throw new IllegalArgumentException(task.getRecord().getState().name());
    }

    private ConversionTaskContext getConversionTaskContext(ConversionTask task) {
        Optional<ConversionTaskContext> context = this.conversionTaskContextRepository.findAllByRecord(task.getRecord());
        return context.orElseGet(() -> ConversionTaskContext.from(task));
    }
}
