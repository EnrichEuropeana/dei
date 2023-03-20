package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.converter.JsonObjectToStringConverter;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
/**
 * This class stores single transcription.
 * This class seems to be a weak entity as it could be identified by only tpId and annotaionId,
 * but on creation there not always is annotationId, thus we need to create surrogate id field
 */
public abstract class AbstractTranscription {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Identifier in TP
     */
    private String tpId;

	@Convert(converter = TranscriptionTypeConverter.class)
	@Column(columnDefinition = "VARCHAR(10) default manual")
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    private TranscriptionType transcriptionType;

    /**
     * Transcribathon item id. Used for retrieving information about the item and its manual or HTR transcriptions
     */
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Record record;

    @Convert(converter = JsonObjectToStringConverter.class)
    @Column(columnDefinition = "LONGTEXT")
    private JsonObject transcriptionContent;

    @JsonProperty("EuropeanaAnnotationId")
    private String annotationId;

    protected AbstractTranscription(Record record) {
        this.record = record;
    }

    protected AbstractTranscription(String tpId, Record record, String annotationId) {
        this.tpId = tpId;
        this.record = record;
        this.annotationId = annotationId;
    }

    public abstract void from(JsonObject transcription);
}
