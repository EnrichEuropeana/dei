package pl.psnc.dei.model.enrichments.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.psnc.dei.model.enrichments.PlaceEnrichment;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class PlaceEnrichmentDTO extends MetadataEnrichmentDTO {
    private final static String WIKIDATA_URL = "https://www.wikidata.org/wiki/%s";

    private final double latitude;

    private final double longitude;

    private final String name;

    private final String language;

    private final String wikiDataURL;

    private final int zoom;

    @Builder
    public PlaceEnrichmentDTO(long id, String attribute, String itemURL, double latitude, double longitude, String name,
            String language, String wikiDataURL, int zoom) {
        super(id, attribute, itemURL);
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.language = language;
        this.wikiDataURL = wikiDataURL;
        this.zoom = zoom;
    }

    public static PlaceEnrichmentDTO from(PlaceEnrichment placeEnrichment) {
        PlaceEnrichmentDTO.PlaceEnrichmentDTOBuilder builder = PlaceEnrichmentDTO.builder()
                .id(placeEnrichment.getId())
                .attribute(placeEnrichment.getAttribute())
                .itemURL(placeEnrichment.getItemLink())
                .name(placeEnrichment.getName())
                .language(placeEnrichment.getLanguage())
                .zoom(placeEnrichment.getZoom())
                .latitude(placeEnrichment.getLatitude())
                .longitude(placeEnrichment.getLongitude());
        Optional.ofNullable(placeEnrichment.getWikidataId()).filter(s -> !"undefined".equals(s))
                .ifPresent(s -> builder.wikiDataURL(String.format(WIKIDATA_URL, s.trim())));
        return builder.build();
    }
}
