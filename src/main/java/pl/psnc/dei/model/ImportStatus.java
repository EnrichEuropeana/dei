package pl.psnc.dei.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Import status:
 * NEW when an import was created but it is not stored in DB yet,
 * CREATED when an import was created and stored in DB and preparation is in progress (records can be added or removed)
 * IN_PROGRESS when sending of the import is in progress
 * FAILED when an import was sent but there was a failure
 * SENT when an import was sent successfully
 */
@Getter
@AllArgsConstructor
public enum ImportStatus {
    NEW(0),
    CREATED(1),
    IN_PROGRESS(2),
    FAILED(3),
    SENT(4);

    private final int status;
}
