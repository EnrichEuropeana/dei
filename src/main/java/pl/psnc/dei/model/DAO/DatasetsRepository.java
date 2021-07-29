package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Dataset;

import java.util.Optional;

/**
 * Created by pwozniak on 3/29/19
 */
@Repository
public interface DatasetsRepository extends JpaRepository<Dataset, Long> {

    Dataset findDatasetByDatasetId(String datasetID);
    Optional<Dataset> findByName(String name);
}
