package pl.psnc.dei.controllers.requests;

import lombok.Data;

import java.util.Set;

@Data
public class MetadataEnrichmentValidation {
    Set<Long> accept;
    Set<Long> reject;
}
