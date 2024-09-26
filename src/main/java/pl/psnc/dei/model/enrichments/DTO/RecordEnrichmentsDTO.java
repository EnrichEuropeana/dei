package pl.psnc.dei.model.enrichments.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pl.psnc.dei.model.enrichments.DateEnrichment;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.model.enrichments.PersonEnrichment;
import pl.psnc.dei.model.enrichments.PlaceEnrichment;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordEnrichmentsDTO {
    String recordId;

    String externalId;

    List<DateEnrichmentDTO> timespans;

    List<PlaceEnrichmentDTO> places;

    List<PersonEnrichmentDTO> persons;

    public static RecordEnrichmentsDTO fromRecordEnrichments(Collection<MetadataEnrichment> enrichments) {
        RecordEnrichmentsDTOBuilder builder = RecordEnrichmentsDTO.builder();
        enrichments.stream().findFirst().ifPresent(metadataEnrichment -> builder.recordId(
                metadataEnrichment.getRecord().getIdentifier()).externalId(metadataEnrichment.getExternalId()));
        builder.timespans(enrichments.stream()
                .filter(metadataEnrichment -> metadataEnrichment instanceof DateEnrichment)
                .map(metadataEnrichment -> DateEnrichmentDTO.from((DateEnrichment) metadataEnrichment)).collect(
                        Collectors.toList()));
        builder.places(enrichments.stream()
                .filter(metadataEnrichment -> metadataEnrichment instanceof PlaceEnrichment)
                .map(metadataEnrichment -> PlaceEnrichmentDTO.from((PlaceEnrichment) metadataEnrichment)).collect(
                        Collectors.toList()));
        builder.persons(enrichments.stream()
                .filter(metadataEnrichment -> metadataEnrichment instanceof PersonEnrichment)
                .map(metadataEnrichment -> PersonEnrichmentDTO.from((PersonEnrichment) metadataEnrichment)).collect(
                        Collectors.toList()));
        return builder.build();
    }
}
