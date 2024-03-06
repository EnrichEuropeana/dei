package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportProgress {

    @Id
    @GeneratedValue
    @JsonIgnore
    private long id;

    private int estimatedTasks;
    private int completedTasks;

    public void incrementCompleted() {
        ++completedTasks;
        if (completedTasks > estimatedTasks) {
            throw new IllegalStateException("Import progress has more completed tasks than maximum!");
        }
    }
}

