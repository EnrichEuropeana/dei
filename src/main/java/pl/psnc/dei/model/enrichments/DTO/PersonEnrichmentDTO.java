package pl.psnc.dei.model.enrichments.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.psnc.dei.model.enrichments.PersonEnrichment;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class PersonEnrichmentDTO extends MetadataEnrichmentDTO {
    private final String firstName;
    private final String lastName;

    private final String birthPlace;
    private final String birthDate;

    private final String deathPlace;
    private final String deathDate;

    private final String language;

    private final String wikiDataURL;

    @Builder
    public PersonEnrichmentDTO(long id, String attribute, TranscribathonItemDTO item,
            String firstName, String lastName, String birthPlace, String birthDate,
            String deathPlace, String deathDate, String language, String wikiDataURL) {
        super(id, attribute, item);
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthPlace = birthPlace;
        this.birthDate = birthDate;
        this.deathPlace = deathPlace;
        this.deathDate = deathDate;
        this.language = language;
        this.wikiDataURL = wikiDataURL;
    }

    public static PersonEnrichmentDTO from(PersonEnrichment personEnrichment) {
        PersonEnrichmentDTO.PersonEnrichmentDTOBuilder builder = PersonEnrichmentDTO.builder()
                .id(personEnrichment.getId())
                .attribute(personEnrichment.getAttribute())
                .firstName(personEnrichment.getFirstName())
                .lastName(personEnrichment.getLastName())
                .birthPlace(personEnrichment.getBirthPlace())
                .birthDate(personEnrichment.getBirthDate())
                .deathPlace(personEnrichment.getDeathPlace())
                .deathDate(personEnrichment.getDeathDate())
                .language(personEnrichment.getLanguage())
                .item(TranscribathonItemDTO.builder().itemURL(personEnrichment.getItemLink())
                        .pageNo(personEnrichment.getPageNo()).build());
        Optional.ofNullable(personEnrichment.getWikidataId()).filter(s -> !"NULL".equals(s))
                .ifPresent(s -> {
                    if (WIKIDATA_ID_PATTERN.matcher(s).matches()) {
                        builder.wikiDataURL(String.format(WIKIDATA_URL, s.trim()));
                    } else {
                        builder.wikiDataURL(s);
                    }
                });
        return builder.build();
    }
}
