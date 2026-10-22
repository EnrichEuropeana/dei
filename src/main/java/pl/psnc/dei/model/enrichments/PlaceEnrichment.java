package pl.psnc.dei.model.enrichments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlaceEnrichment extends MetadataEnrichment {
	private double latitude;
	private double longitude;
	private String name;
	private String language;
	private String wikidataId;
	private int zoom;
}