package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pl.psnc.dei.converter.RecordStateConverter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Record {

    @Id
    @GeneratedValue
    private long id;

	/**
	 * Record identifier (from Europeana)
	 * Looks like: "[DATASET_ID]/[LOCAL_ID]"
	 */
	private String identifier;

    @Convert(converter = RecordStateConverter.class)
	@Column(columnDefinition = "int default 0")
    private RecordState state;

	@JsonIgnore
    @ManyToOne
    private Project project;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Dataset dataset;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Import anImport;

    @JsonIgnore
    @OneToMany(orphanRemoval = true)
    private List<Transcription> transcriptions;

    @JsonIgnore
    private String iiifManifest;

    public Record() {
    }

    public Record(String identifier) {
        this.identifier = identifier;
    }

	public Record(String identifier, RecordState state, Project project, Dataset dataset, Import anImport, List<Transcription> transcriptions, String iiifManifest) {
		this.identifier = identifier;
		this.state = state;
		this.project = project;
		this.dataset = dataset;
		this.anImport = anImport;
		this.transcriptions = transcriptions;
		this.iiifManifest = iiifManifest;
	}

	public long getId() {
		return id;
	}

	public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public RecordState getState() {
        return state;
    }

    public void setState(RecordState state) {
        this.state = state;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Import getAnImport() {
        return anImport;
    }

    public void setAnImport(Import anImport) {
        this.anImport = anImport;
    }

	public List<Transcription> getTranscriptions() {
		return transcriptions;
	}

	public void setTranscriptions(List<Transcription> transcriptions) {
		this.transcriptions = transcriptions;
	}

	public String getIiifManifest() {
		return iiifManifest;
	}

	public void setIiifManifest(String iiifManifest) {
		this.iiifManifest = iiifManifest;
	}

	/**
	 * States representing record state, meanings:
	 * NORMAL - no action needed, just a normal record
	 * E_PENDING - Enrichment process for given record is pending, transcriptions are ready to be taken from TP to EU
	 * T_PENDING - Transcription process for given record is pending, records will be transferred from EU to TP
	 * U_PENDING - Update process for given record annotations is pending
	 * C_PENDING - Non IIIF record, needs to bo converted before sending to TP
	 * C_FAILED - Record isn't in IIIF, and record conversion failed
	 */
	public enum RecordState {

		NORMAL(0),
		E_PENDING(1),
		T_PENDING(2),
		U_PENDING(3),
		C_PENDING(4),
		C_FAILED(5);

		private final int value;

		RecordState(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		private static final Map<Integer, RecordState> map = new HashMap<>();

		static {
			for(RecordState state : RecordState.values())
				map.put(state.value, state);
		}

		public static RecordState getState(int value) {
			return map.get(value);
		}
	}
}
