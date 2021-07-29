package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.jena.atlas.json.JsonObject;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
/**
 * This class stores single transcription.
 * This class seems to be a weak entity as it could be identified by only tpId and annotaionId,
 * but on creation there not always is annotationId, thus we need to create surrogate id field
 */
public class Transcription {

	@Id
	@GeneratedValue
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private long id;

	private String tpId;

	@ManyToOne(fetch = FetchType.LAZY)
	private Record record;

	@Transient
	private JsonObject transcriptionContent;

	@JsonProperty("EuropeanaAnnotationId")
	private String annotationId;


	public Transcription(String tpId, Record record, String annotationId) {
		this.tpId = tpId;
		this.record = record;
		this.annotationId = annotationId;
	}
}