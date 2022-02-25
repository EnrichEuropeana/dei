package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import pl.psnc.dei.converter.AggregatorConverter;
import pl.psnc.dei.converter.RecordStateConverter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Record {

	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Record identifier from aggregator (Europeana or Deutsche Digitale Bibliothek)
	 * Europeana id looks like: "/[DATASET_ID]/[LOCAL_ID]"
	 */
	private String identifier;

	@Convert(converter = RecordStateConverter.class)
	@Column(columnDefinition = "int default 0")
	private RecordState state;

	@JsonIgnore
	@ManyToOne
	@Cascade(value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE})
	private Project project;

	@JsonIgnore
	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	private Dataset dataset;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	private Import anImport;

	@JsonIgnore
	@OneToMany(cascade = {CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "record")
	private List<Transcription> transcriptions;

	@JsonIgnore
	@Lob
	private String iiifManifest;

	@JsonIgnore
	@Convert(converter = AggregatorConverter.class)
	private Aggregator aggregator;

	@JsonIgnore
	@Lob
	private String title;

	public Record(String identifier, String title) {
		this.identifier = identifier;
		this.title = title;
	}

	public Record(String identifier) {
		this.identifier = identifier;
	}

	public Record(String identifier, RecordState state, Project project, Dataset dataset, Import anImport, List<Transcription> transcriptions, String iiifManifest, Aggregator aggregator, String title) {
		this.identifier = identifier;
		this.state = state;
		this.project = project;
		this.dataset = dataset;
		this.anImport = anImport;
		this.transcriptions = transcriptions;
		this.iiifManifest = iiifManifest;
		this.aggregator = aggregator;
		this.title = title;
	}

	/**
	 * States representing record state, meanings:
	 * NORMAL - no action needed, just a normal record
	 * E_PENDING - Enrichment process for given record is pending, transcriptions are ready to be taken from TP to EU
	 * T_PENDING - Transcription process for given record is pending, records will be transferred from EU to TP
	 * U_PENDING - Update process for given record annotations is pending
	 * C_PENDING - Non IIIF record, needs to bo converted before sending to TP
	 * C_FAILED - Record isn't in IIIF, and record conversion failed
	 * T_FAILED - Transcription process has started and there was an attempt to transfer the record to TP which failed
	 * T_SENT - Transcription process has started and the record was successfully transferred to TP
	 */
	@Getter
	@AllArgsConstructor
	public enum RecordState {

		NORMAL(0),
		E_PENDING(1),
		T_PENDING(2),
		U_PENDING(3),
		C_PENDING(4),
		C_FAILED(5),
		T_FAILED(6),
		T_SENT(7);


		private static final Map<Integer, RecordState> map = new HashMap<>();

		static {
			for (RecordState state : RecordState.values())
				map.put(state.value, state);
		}

		private final int value;

		public static RecordState getState(int value) {
			return map.get(value);
		}
	}
}
