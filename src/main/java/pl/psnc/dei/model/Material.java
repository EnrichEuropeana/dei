package pl.psnc.dei.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Material {
    @Id
    @GeneratedValue
    private long id;

    private String identifier;

    public Material(long id, String identifier) {
        this.id = id;
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
}
