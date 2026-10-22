package pl.psnc.dei.model.DAO;

import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.enrichments.PlaceEnrichment;
import pl.psnc.dei.model.Record;

import java.util.Optional;

@Repository
public interface PlaceEnrichmentRepository extends MetadataEnrichmentRepository<PlaceEnrichment> {

    Optional<PlaceEnrichment> findByRecordAndLatitudeAndLongitudeAndNameAndItemLink(Record record, double latitude, double longitude, String name, String itemLink);

    boolean existsByRecordAndLatitudeAndLongitudeAndNameAndItemLink(Record record, double latitude, double longitude, String name, String itemLink);
}
