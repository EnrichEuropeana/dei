package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ValidationTaskContext;

import java.util.Optional;

public interface ValidationTaskContextRepository extends CrudRepository<ValidationTaskContext, Long> {
    Optional<ValidationTaskContext> findByRecord(Record record);
}
