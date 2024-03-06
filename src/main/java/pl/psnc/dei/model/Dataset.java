package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Describes Dataset entity taken from the Transcription Platform
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dataset implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    @JsonProperty("DatasetId")
    private String datasetId;

    @JsonProperty("Name")
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    private Project project;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "dataset")
    private List<Record> records;

    @Override
    public String toString() {
        return this.name;
    }
}
