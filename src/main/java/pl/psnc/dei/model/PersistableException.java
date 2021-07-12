package pl.psnc.dei.model;

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
}
