package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
