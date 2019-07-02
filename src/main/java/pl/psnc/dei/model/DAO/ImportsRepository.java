package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Import;

import java.util.Optional;

@Repository
public interface ImportsRepository extends JpaRepository<Import, Long> {
    Optional<Import> findImportByName(String importName);
}
