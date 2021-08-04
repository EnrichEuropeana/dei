package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Transcription;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
    List<Transcription> findAllByTpId(String tpId);

    boolean existsByRecord_Identifier(String recordId);

    boolean existsByTpId(String tpId);

    boolean existsByTpIdAndAnnotationId(String tpId, String annotationId);

    Optional<Transcription> findByTpId(String tpId);
}
