package pl.psnc.dei.model.DAO;


import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionData;

import java.util.Optional;

public interface ConversionContextRepository extends CrudRepository<ConversionData, Long> {
}
