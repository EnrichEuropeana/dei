package pl.psnc.dei.model.enrichments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DateEnrichment extends MetadataEnrichment {
	private Instant dateStart;

	private Instant dateEnd;

}