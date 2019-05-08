package pl.psnc.dei.model;

import org.apache.jena.atlas.json.JsonObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Transcription {

	@Id
	@GeneratedValue
	private long id;

	private String tp_id;

	@ManyToOne
	private Record record;

	private JsonObject transcriptionContent;

	private String annotationId;

	public Transcription() {
	}

	public Transcription(String tp_id, Record record, String annotationId) {
		this.tp_id = tp_id;
		this.record = record;
		this.annotationId = annotationId;
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
}
