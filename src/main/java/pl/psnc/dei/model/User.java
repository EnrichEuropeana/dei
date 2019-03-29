package pl.psnc.dei.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class User {
    @Id
    private long id;

    @NotNull
    private String username;

    public User() {
    }

    public User(long id, String username) {
        this.id = id;
        this.username = username;

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

}

