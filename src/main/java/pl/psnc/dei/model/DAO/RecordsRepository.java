package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RecordsRepository extends JpaRepository<Record, Long> {

    List<Record> findAllByProject(Project project);

    List<Record> findAllByDataset(Dataset dataset);

    List<Record> findAllByProjectAndDatasetNullAndAnImportNull(Project project);

    Set<Record> findAllByProjectAndAnImportNull(Project project);

    Set<Record> findAllByProjectAndDatasetAndAnImportNull(Project project, Dataset dataset);

    Record findByIdentifierAndProject(String identifier, Project project);

    Record findByIdentifierAndProjectAndDataset(String identifier, Project project, Dataset dataset);

    Set<Record> findAllByAnImport(Import anImport);

    Optional<Record> findByIdentifier(String id);

    List<Record> findAllByStateIsNotIn(List<Record.RecordState> states);

}
