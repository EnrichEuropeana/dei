package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.model.converter.JsonObjectToStringConverter;

import javax.persistence.*;

@Entity
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

	private String annotationId;

	public Transcription() {
	}

	public Transcription(String tp_id, Record record, String annotationId) {
		this.tp_id = tp_id;
		this.record = record;
		this.annotationId = annotationId;
		this.transcriptionContent = new JsonObject();
	}

	public String getTp_id() {
		return tp_id;
	}

	public void setTp_id(String tp_id) {
		this.tp_id = tp_id;
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	@JsonProperty("EuropeanaAnnotationId")
	public String getAnnotationId() {
		return annotationId;
	}

	public void setAnnotationId(String annotationId) {
		this.annotationId = annotationId;
	}

	public JsonObject getTranscriptionContent() {
		return transcriptionContent;
	}

	public void setTranscriptionContent(JsonObject transcriptionContent) {
		this.transcriptionContent = transcriptionContent;
	}
//
//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
}
