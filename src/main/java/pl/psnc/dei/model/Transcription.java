package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.jena.atlas.json.JsonObject;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Transcription {

	@EmbeddedId
	private final TranscriptionPK key;
	@ManyToOne(fetch = FetchType.LAZY)
	@Getter
	@Setter
	private Record record;
	@Transient
	@Getter
	@Setter
	private JsonObject transcriptionContent;

	public Transcription(String tp_id, Record record, String annotationId) {
		this.key = new TranscriptionPK(tp_id, annotationId);
		this.record = record;
	}

	public Transcription() {
		this.key = new TranscriptionPK();
	}

	public String getTp_id() {
		return this.key.getTp_id();
	}

	public void setTp_id(String tp_id) {
		this.key.setTp_id(tp_id);
	}

	@JsonProperty("EuropeanaAnnotationId")
	public String getAnnotationId() {
		return this.key.annotationId;
	}

	public void setAnnotationId(String annotationId) {
		this.key.setAnnotationId(annotationId);
	}

	@Data
	@Embeddable
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TranscriptionPK implements Serializable {
		@Column(length = 120)
		private String tp_id;
		@Column(length = 120)
		private String annotationId;
	}
}
