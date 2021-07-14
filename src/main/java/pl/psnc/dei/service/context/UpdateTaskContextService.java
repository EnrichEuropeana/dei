package pl.psnc.dei.service.context;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.DAO.UpdateTaskContextRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.UpdateTaskContext;
import pl.psnc.dei.queue.task.Task;

import java.util.Optional;

@Service
public class UpdateTaskContextService extends ContextService<UpdateTaskContext>{
    private final UpdateTaskContextRepository updateTaskContextRepository;

    public UpdateTaskContextService(UpdateTaskContextRepository updateTaskContextRepository) {
        this.updateTaskContextRepository = updateTaskContextRepository;
    }

    @Override
    public UpdateTaskContext get (Record record) {
        Optional<UpdateTaskContext> context = this.updateTaskContextRepository.findByRecord(record);
        return context.orElseGet(() -> UpdateTaskContext.from(record));
    }

    @Override
    public UpdateTaskContext save(UpdateTaskContext context) {
        return this.updateTaskContextRepository.save(context);
    }

    @Override
    public void delete(UpdateTaskContext context) {
        this.updateTaskContextRepository.delete(context);
    }

    @Override
    public Boolean canHandle(Record record) {
        return record.getState().equals(Record.RecordState.U_PENDING);
    }

    @Override
    public Boolean canHandle(Class<?> aClass) {
        return aClass.isAssignableFrom(UpdateTaskContext.class);
    }
}
