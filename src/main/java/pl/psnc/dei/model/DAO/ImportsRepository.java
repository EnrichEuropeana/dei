package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Import;

@Repository
public interface ImportsRepository extends JpaRepository<Import, Long> {
    Import getImportByName(String name);
}
