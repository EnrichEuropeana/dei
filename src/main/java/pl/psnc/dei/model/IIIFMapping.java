package pl.psnc.dei.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IIIFMapping {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    private Record record;

    private int orderIndex;
    private String sourceUrl;
    private String iiifResourceUrl;
}
