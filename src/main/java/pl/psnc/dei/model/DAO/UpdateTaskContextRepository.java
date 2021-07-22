package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.UpdateTaskContext;

import java.util.Optional;

public interface UpdateTaskContextRepository extends CrudRepository<UpdateTaskContext, Long> {
    Optional<UpdateTaskContext> findByRecord(Record record);
}
