package pl.psnc.dei.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;

import java.util.Set;

@NoRepositoryBean
public interface MetadataEnrichmentRepository<T extends MetadataEnrichment> extends JpaRepository<T, Long> {
    Set<MetadataEnrichment> findAllByExternalIdAndState(String recordId, MetadataEnrichment.EnrichmentState state);

    Set<MetadataEnrichment> findAllByExternalIdAndStateAndIdIn(String recordId, MetadataEnrichment.EnrichmentState state, Set<Long> ids);

    Set<MetadataEnrichment> findAllByRecordAndState(Record record, MetadataEnrichment.EnrichmentState state);

    Set<MetadataEnrichment> findAllByRecordAndStateAndIdIn(Record record, MetadataEnrichment.EnrichmentState state, Set<Long> ids);

    Set<MetadataEnrichment> findAllByRecordId(String recordId);

    Set<MetadataEnrichment> findAllByRecordIdContaining(String domain);

    Set<MetadataEnrichment> findAllByExternalIdContainingAndState(String domain, MetadataEnrichment.EnrichmentState state);

    Set<Record> findRecordsByRecordIdContaining(String domain);

}
