package pl.psnc.dei.model.conversion;

import org.hibernate.annotations.NaturalId;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.queue.task.ConversionTask;

import javax.persistence.*;

/**
 * Stores context for conversion task
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ConversionTaskContext extends Context{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @OneToOne
    private Record record;

    // PROCESSING STATE STORAGE
    private boolean hasJson;
    private boolean hasConverted;
    private boolean hasThrownException;
    private boolean hasAddedFailure;

    // PROCESSING DATA STORAGE
    private String recordJsonRaw;
    private String recordJson;
    private Exception exception;


    public static ConversionTaskContext from(ConversionTask task){
        ConversionTaskContext context = new ConversionTaskContext();
        context.setRecord(task.getRecord());
        context.setHasJson(false);
        context.setHasConverted(false);
        context.setHasThrownException(false);
        context.setHasAddedFailure(false);
        context.setRecordJson("");
        context.setRecordJsonRaw("");
        context.setException(null);
        return context;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public void setRecord(Record record) {
        this.record = record;
    }

    public boolean isHasJson() {
        return hasJson;
    }

    public void setHasJson(boolean hasJson) {
        this.hasJson = hasJson;
    }

    public boolean isHasConverted() {
        return hasConverted;
    }

    public void setHasConverted(boolean hasConverted) {
        this.hasConverted = hasConverted;
    }

    public boolean isHasThrownException() {
        return hasThrownException;
    }

    public void setHasThrownException(boolean hasThrownException) {
        this.hasThrownException = hasThrownException;
    }

    public boolean isHasAddedFailure() {
        return hasAddedFailure;
    }

    public void setHasAddedFailure(boolean hasAddedFailure) {
        this.hasAddedFailure = hasAddedFailure;
    }

    public String getRecordJsonRaw() {
        return recordJsonRaw;
    }

    public void setRecordJsonRaw(String recordJsonRaw) {
        this.recordJsonRaw = recordJsonRaw;
    }

    public String getRecordJson() {
        return recordJson;
    }

    public void setRecordJson(String recordJson) {
        this.recordJson = recordJson;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
