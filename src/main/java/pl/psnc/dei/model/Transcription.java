package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.jena.atlas.json.JsonObject;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Transcription {

	@Id
	@GeneratedValue
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private long id;

	private String tp_id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Record record;

	@Transient
	private JsonObject transcriptionContent;

	@JsonProperty("EuropeanaAnnotationId")
	private String annotationId;


	public Transcription(String tp_id, Record record, String annotationId) {
		this.tp_id = tp_id;
		this.record = record;
		this.annotationId = annotationId;
	}
}
