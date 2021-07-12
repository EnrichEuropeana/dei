package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.PersistableException;

public interface PersistableExceptionDAO extends CrudRepository<PersistableException, Long> {
}
