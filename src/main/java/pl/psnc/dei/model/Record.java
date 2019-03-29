package pl.psnc.dei.model;

import javax.persistence.*;

@Entity
public class Record {

    @Id
    @GeneratedValue
    private long id;

    private String identifier;

    @ManyToOne
    private Project project;

    @ManyToOne
    private Dataset dataset;

    public Record() {
    }

    public Record(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public long getId() {
        return id;
    }


    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
}
