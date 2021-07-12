package pl.psnc.dei.model;

import pl.psnc.dei.model.conversion.Context;

import javax.persistence.*;

@Entity
public class PersistableException {

    public enum ExceptionType{
        TRANSCRIPTION_PLATFORM_EXCEPTION
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ExceptionType type;
    @Column(columnDefinition = "TEXT")
    private String message;

    @ManyToOne
    private Context context;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExceptionType getType() {
        return type;
    }

    public void setType(ExceptionType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
