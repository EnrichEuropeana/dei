package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Transcription;

@Repository
public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
    boolean existsByRecord_Identifier(String recordId);
}
