package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Project;

/**
 * Created by pwozniak on 3/29/19
 */
@Repository
public interface ProjectsRepository extends JpaRepository<Project, Long> {

    Project findByName(String name);

    Project findByProjectId(String projectId);
}
