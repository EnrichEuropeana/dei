package pl.psnc.dei.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Campaign {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @ManyToOne
    private User owner;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "campaign")
    private List<Record> records;

    public Campaign() {
    }

    public Campaign(String name, List<Record> records) {
        this.name = name;
        this.records = records;
    }
    public Campaign(String name) {
        this.name = name;
        this.records = new ArrayList<>();
    }

    public void addRecord(Record record){
        records.add(record);
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

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
