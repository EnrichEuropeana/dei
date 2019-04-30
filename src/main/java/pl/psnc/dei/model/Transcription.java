package pl.psnc.dei.model;

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

	private String annotationId;

	private String transcription;

	private String target;

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

	public String getTranscription() {
		return transcription;
	}

	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
