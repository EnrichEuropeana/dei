package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.converter.JsonObjectToStringConverter;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

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
    private Long id;

    private String tpId;

	@Convert(converter = TranscriptionTypeConverter.class)
	@Column(columnDefinition = "VARCHAR(10) default manual")
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


    public Transcription(String tpId, Record record, String annotationId) {
        this.tpId = tpId;
        this.record = record;
        this.annotationId = annotationId;
        this.transcriptionContent = new JsonObject();
    }

    public enum TranscriptionType {
        MANUAL,
        HTR;

        public static TranscriptionType from(String type) {
            Objects.requireNonNull(type);
            return Arrays.stream(values()).filter(value -> value.toString().equalsIgnoreCase(type)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown transcription type %s", type)));
        }
    }
}
