package pl.psnc.dei.model.DAO;

import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.enrichments.DateEnrichment;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface DateEnrichmentRepository extends MetadataEnrichmentRepository<DateEnrichment> {

    Optional<DateEnrichment> findByRecordAndDateEndAndDateStartAndItemLink(Record record, Instant end, Instant start, String itemLink);

    boolean existsByRecordAndDateEndAndDateStartAndItemLink(Record record, Instant end, Instant start, String itemLink);
}
