package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.MetadataEnrichTaskContext;

import java.util.Optional;

public interface MetadataEnrichTaskContextRepository extends CrudRepository<MetadataEnrichTaskContext, Long> {
    Optional<MetadataEnrichTaskContext> findByRecord(Record record);
}
