package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ImportFailure {
    @Id
    @GeneratedValue
    @JsonIgnore
    private long id;

    private Date occurenceDate;

    @Lob
    private String reason;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    private Import anImport;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getOccurenceDate() {
        return occurenceDate;
    }

    public void setOccurenceDate(Date occurenceDate) {
        this.occurenceDate = occurenceDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Import getAnImport() {
        return anImport;
    }

    public void setAnImport(Import anImport) {
        this.anImport = anImport;
    }
}
