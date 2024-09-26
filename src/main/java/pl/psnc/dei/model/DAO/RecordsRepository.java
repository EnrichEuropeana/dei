package pl.psnc.dei.model.DAO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RecordsRepository extends PagingAndSortingRepository<Record, Long> {

    Set<Record> findAllByProject(Project project);

    Set<Record> findAllByDataset(Dataset dataset);

    List<Record> findAllByProjectAndDatasetNullAndAnImportNull(Project project);

    Set<Record> findAllByProjectAndAnImportNull(Project project);

    Set<Record> findAllByProjectAndDatasetAndAnImportNull(Project project, Dataset dataset);

    Record findByIdentifierAndProject(String identifier, Project project);

    Record findByIdentifierAndProjectAndDataset(String identifier, Project project, Dataset dataset);

    Set<Record> findAllByAnImport(Import anImport);

    Set<Record> findAllByAnImportAndStateIsIn(Import anImport, List<Record.RecordState> states);

    Set<Record> findAllByAnImportAndState(Import anImport, Record.RecordState state);

    Optional<Record> findByIdentifier(String id);

    Optional<Record> findByIdentifierAndIiifManifestNotNull(String id);

    List<Record> findAllByStateIsNotIn(List<Record.RecordState> states);

    Set<Record> findAllByAnImportNull();

    Page<Record> findAllByStoryIdNull(Pageable pageable);

    Page<Record> findAllByStoryIdNotNull(Pageable pageable);

    Page<Record> findAllByIiifManifestNotNull(Pageable pageable);

    Set<Record> findAllByAggregatorAndAnImportNull(Aggregator aggregator);

    Set<Record> findAllByProjectAndAggregatorAndAnImportNull(Project project, Aggregator aggregator);

    Set<Record> findRecordsByIdentifierInAndAnImportNotNull(List<String> identifiers);

}
