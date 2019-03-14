package pl.psnc.dei.model;

import javax.persistence.*;

@Entity
public class Record {

    @Id
    @GeneratedValue
    private long id;

    private String identifier;

    @ManyToOne
    private Campaign campaign;

    public Record(){}

    public Record(String identifier) {
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

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Campaign getCampaign() {
        return campaign;
    }
}
