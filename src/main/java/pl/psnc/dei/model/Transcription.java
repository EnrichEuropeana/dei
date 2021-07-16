package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.converter.JsonObjectToStringConverter;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Transcription {

	@Id
	@GeneratedValue
	private Long id;

	private String tp_id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Record record;

	@Convert(converter = JsonObjectToStringConverter.class)
	@Column(columnDefinition = "LONGTEXT")
	private JsonObject transcriptionContent;

	@JsonProperty("EuropeanaAnnotationId")
	private String annotationId;


	public Transcription(String tp_id, Record record, String annotationId) {
		this.tp_id = tp_id;
		this.record = record;
		this.annotationId = annotationId;
		this.transcriptionContent = new JsonObject();
	}
}
