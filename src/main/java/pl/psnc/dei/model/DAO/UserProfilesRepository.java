package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.UserProfile;

@Repository
public interface UserProfilesRepository extends JpaRepository<UserProfile, Long> {
}
