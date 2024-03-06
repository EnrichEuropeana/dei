package pl.psnc.dei.model.conversion;

import org.hibernate.annotations.NaturalId;
import pl.psnc.dei.model.PersistableExceptionEntity;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.queue.task.Task;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Context {
    @Id @GeneratedValue
    private Long id;

    @NaturalId
    @OneToOne
    private Record record;

    @OneToMany(mappedBy = "context", cascade = CascadeType.ALL)
    private List<PersistableExceptionEntity> exceptions;

    private Task.TaskState taskState;

    public Task.TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(Task.TaskState taskState) {
        this.taskState = taskState;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<PersistableExceptionEntity> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<PersistableExceptionEntity> exceptions) {
        this.exceptions = exceptions;
    }
}
