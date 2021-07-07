package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
