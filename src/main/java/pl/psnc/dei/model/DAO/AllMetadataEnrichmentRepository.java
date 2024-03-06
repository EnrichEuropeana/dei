package pl.psnc.dei.model.DAO;

import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;

@Repository
public interface AllMetadataEnrichmentRepository extends MetadataEnrichmentRepository<MetadataEnrichment> {
}
