package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.conversion.ConversionData;

public interface ConversionDataRepository extends CrudRepository<ConversionData, Long> {
}
