package pl.psnc.dei.model;

/**
 * Import status:
 * NEW when an import was created but it is not stored in DB yet,
 * CREATED when an import was created and stored in DB and preparation is in progress (records can be added or removed)
 * IN_PROGRESS when sending of the import is in progress
 * FAILED when an import was sent but there was a failure
 * SENT when an import was sent successfully
 */
public enum ImportStatus {
    NEW(1),
    CREATED(2),
    IN_PROGRESS(3),
    FAILED(4),
    SENT(5);

    private final int status;

    ImportStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
