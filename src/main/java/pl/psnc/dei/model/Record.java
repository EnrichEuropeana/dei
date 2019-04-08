package pl.psnc.dei.model;

import javax.persistence.*;

@Entity
public class Record {

    @Id
    @GeneratedValue
    private long id;

    private String identifier;

    @ManyToOne(cascade = CascadeType.ALL)
    private Project project;

    @ManyToOne(cascade = CascadeType.ALL)
    private Dataset dataset;

    @ManyToOne(cascade = CascadeType.ALL)
    private Import anImport;

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

    public Import getAnImport() {
        return anImport;
    }

    public void setAnImport(Import anImport) {
        this.anImport = anImport;
    }
}
