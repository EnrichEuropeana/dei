package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import java.util.List;

@Repository
public interface RecordsRepository extends JpaRepository<Record, Long> {

    List<Record> findAllByProject(Project project);

    List<Record> findAllByDataset(Dataset dataset);

    Record findByIdentifierAndProject(String identifier, Project project);

    Record findByIdentifierAndProjectAndDataset(String identifier, Project project, Dataset dataset);
}
