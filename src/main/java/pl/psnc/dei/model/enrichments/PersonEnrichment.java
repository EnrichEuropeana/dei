package pl.psnc.dei.model.enrichments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonEnrichment extends MetadataEnrichment {
	private String firstName;
	private String lastName;
	private String birthPlace;
	private String birthDate;
	private String deathPlace;
	private String deathDate;
	private String language;
	private String wikidataId;
}