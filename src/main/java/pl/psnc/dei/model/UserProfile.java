package pl.psnc.dei.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    private long id;

    @NotNull
    private String username;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Campaign> collections;


    public UserProfile(long id, String username, List<Campaign> collections) {
        this.id = id;
        this.username = username;
        this.collections = collections;
    }


    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Campaign> getCollections() {
        return collections;
    }

    public void setCollections(List<Campaign> collections) {
        this.collections = collections;
    }
}
