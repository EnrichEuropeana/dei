package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Describes Dataset entity taken from the Transcription Platform
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Entity
public class Dataset implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    @JsonProperty("DatasetId")
    private String datasetId;

    @JsonProperty("Name")
    private String name;

    @ManyToOne
    private Project project;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "dataset")
    private List<Record> records;

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }


    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
