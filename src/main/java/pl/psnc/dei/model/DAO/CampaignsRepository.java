package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Campaign;

@Repository
public interface CampaignsRepository extends JpaRepository<Campaign, Long> {
}
