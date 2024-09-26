package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    public void buildReason(String recordName, Throwable exception) {
        String reason = "Failed record: " + recordName +
                ", message: " + exception.getMessage();
        if (exception.getCause() != null) {
            reason += ", caused by: " + exception.getCause().getMessage();
        }
        setReason(reason);
    }
}
