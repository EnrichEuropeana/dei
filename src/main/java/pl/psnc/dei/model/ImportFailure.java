package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
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
