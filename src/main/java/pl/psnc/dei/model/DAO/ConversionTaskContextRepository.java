package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.util.Optional;

public interface ConversionTaskContextRepository extends CrudRepository<ConversionTaskContext, Long> {
    Optional<ConversionTaskContext> findAllByRecord(Record record);
}
