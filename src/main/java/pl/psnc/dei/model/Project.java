package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes Project entity taken from the Transcription Platform
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Entity
public class Project implements Serializable {

    @Id
    @GeneratedValue
    @JsonIgnore
    private long id;

    @JsonProperty("ProjectId")
    private String projectId;

    @JsonProperty("Name")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
    private List<Dataset> datasets = new ArrayList<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
    private List<Record> records;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
