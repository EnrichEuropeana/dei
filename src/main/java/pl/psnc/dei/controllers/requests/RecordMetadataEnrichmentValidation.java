package pl.psnc.dei.controllers.requests;

import lombok.Data;

@Data
public class RecordMetadataEnrichmentValidation {
    String recordId;
    String externalId;
    MetadataEnrichmentValidation timespans;
    MetadataEnrichmentValidation places;
}
