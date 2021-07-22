package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.TranscribeTaskContext;

import java.util.Optional;

public interface TranscribeTaskContextRepository extends CrudRepository<TranscribeTaskContext, Long> {
    Optional<TranscribeTaskContext> findByRecord(Record record);
}
