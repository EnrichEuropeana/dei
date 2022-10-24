package pl.psnc.dei.model.enrichments.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.psnc.dei.model.enrichments.DateEnrichment;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class DateEnrichmentDTO extends MetadataEnrichmentDTO {
    private final String begin;

    private final String end;

    @Builder
    public DateEnrichmentDTO(long id, String attribute, String itemURL, String begin, String end) {
        super(id, attribute, itemURL);
        this.begin = begin;
        this.end = end;
    }

    public static DateEnrichmentDTO from(DateEnrichment dateEnrichment) {
        DateEnrichmentDTOBuilder builder =  DateEnrichmentDTO.builder()
                .id(dateEnrichment.getId())
                .attribute(dateEnrichment.getAttribute());
        Optional.ofNullable(dateEnrichment.getItemLink()).ifPresent(builder::itemURL);
        Optional.ofNullable(dateEnrichment.getDateStart()).ifPresent(instant -> builder.begin(LocalDate.ofInstant(instant, ZoneId.of("UTC")).toString()));
        Optional.ofNullable(dateEnrichment.getDateEnd()).ifPresent(instant -> builder.end(LocalDate.ofInstant(instant, ZoneId.of("UTC")).toString()));
        return builder.build();
    }
}
