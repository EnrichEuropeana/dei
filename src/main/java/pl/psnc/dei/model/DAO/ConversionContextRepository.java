package pl.psnc.dei.model.DAO;


import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionContext;

import java.util.Optional;

public interface ConversionContextRepository extends CrudRepository<ConversionContext, Long> {
    Optional<ConversionContext> findByRecord(Record record);
}
