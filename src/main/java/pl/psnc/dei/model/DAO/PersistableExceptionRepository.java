package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.PersistableException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;

import java.util.List;
import java.util.Optional;

public interface PersistableExceptionRepository extends CrudRepository<PersistableException, Long> {
    Optional<PersistableException> findByContextAndType(Context context, PersistableException.ExceptionType type);
}
