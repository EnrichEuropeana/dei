package pl.psnc.dei.model.enrichments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.psnc.dei.converter.MetadataEnrichmentStateConverter;
import pl.psnc.dei.model.Record;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@NoArgsConstructor
/**
 * This class is for metadata enrichment.
 */
public abstract class MetadataEnrichment {

	@Id
	@GeneratedValue
	private Long id;

	@Convert(converter = MetadataEnrichmentStateConverter.class)
	@Column(columnDefinition = "int default 0")
	private MetadataEnrichment.EnrichmentState state;

	@ManyToOne(fetch = FetchType.LAZY)
	private Record record;

	/** This identifier is OAI-PMH identifier or DOI identifier.
	 * It is stored here in addition to the identifier inside Record because OAI-PMH or DOI identifiers
	 * have to be extracted from the record content */
	private String externalId;

	private String attribute;

	private String itemLink;

	@Column(columnDefinition = "int default 0")
	private int pageNo;

	@Getter
	@AllArgsConstructor
	public enum EnrichmentState {

		PENDING(0),
		ACCEPTED(1),
		REJECTED(2);


		private static final Map<Integer, MetadataEnrichment.EnrichmentState> map = new HashMap<>();

		static {
			for (MetadataEnrichment.EnrichmentState state : MetadataEnrichment.EnrichmentState.values())
				map.put(state.value, state);
		}

		private final int value;

		public static MetadataEnrichment.EnrichmentState getState(int value) {
			return map.get(value);
		}

		public static MetadataEnrichment.EnrichmentState fromString(String value) { return map.get(Integer.parseInt(value)); }
	}
}