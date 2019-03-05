package pl.psnc.dei.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Campaign {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Material> selected;

    public Campaign() {

    }

    public Campaign(long id, String name, List<Material> selected) {
        this.id = id;
        this.name = name;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public List<Material> getSelected() {
        return selected;
    }

    public void setSelected(List<Material> selected) {
        this.selected = selected;
    }
}
