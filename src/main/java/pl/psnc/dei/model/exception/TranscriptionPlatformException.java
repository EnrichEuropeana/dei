package pl.psnc.dei.model.exception;

/**
 * Created by pwozniak on 4/26/19
 */
public class TranscriptionPlatformException extends RuntimeException {

    public TranscriptionPlatformException() {}

    public TranscriptionPlatformException(String message) {
        super(message);
    }

    public TranscriptionPlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}
