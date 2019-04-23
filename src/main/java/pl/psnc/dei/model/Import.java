package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
public class Import {
    @Id
    @GeneratedValue
    @JsonIgnore
    private long id;

    private String name;

    private Date creationDate;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "anImport")
    private Set<Record> records;

    private ImportStatus status;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "anImport")
    private Set<ImportFailure> failures;

    public static Import from(String name, Date date) {
        Import anImport = new Import();
        anImport.setName(name);
        anImport.setCreationDate(date);
        return anImport;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<Record> getRecords() {
        return records;
    }

    public void setRecords(Set<Record> records) {
        this.records = records;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public void setStatus(ImportStatus status) {
        this.status = status;
    }

    public Set<ImportFailure> getFailures() {
        return failures;
    }

    public void setFailures(Set<ImportFailure> failures) {
        this.failures = failures;
    }
}
