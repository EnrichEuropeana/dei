package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.EnrichTaskContext;

import java.util.Optional;

public interface EnrichTaskContextRepository extends CrudRepository<EnrichTaskContext, Long> {
    Optional<EnrichTaskContext> findByRecord(Record record);
}
