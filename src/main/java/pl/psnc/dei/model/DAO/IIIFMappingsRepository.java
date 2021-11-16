package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.IIIFMapping;

import java.util.Optional;

@Repository
public interface IIIFMappingsRepository extends JpaRepository<IIIFMapping, Long> {

    Optional<IIIFMapping> findById(Long id);

    Optional<IIIFMapping> findIIIFMappingByRecordIdentifierAndOrderIndex(String recordIdentifier, int orderIndex);
}
