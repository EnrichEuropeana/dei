package pl.psnc.dei.model.DAO;

import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.PersistableExceptionEntity;
import pl.psnc.dei.model.conversion.Context;

import java.util.Optional;

public interface PersistableExceptionRepository extends CrudRepository<PersistableExceptionEntity, Long> {
    Optional<PersistableExceptionEntity> findByContextAndType(Context context, PersistableExceptionEntity.ExceptionType type);
}
