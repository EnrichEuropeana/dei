package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Material;

@Repository
public interface MaterialsRepository extends JpaRepository<Material, Long> {
}
