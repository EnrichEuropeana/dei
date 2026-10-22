package pl.psnc.dei.exception;

public class TranscriptionDuplicationException extends Exception {
    public TranscriptionDuplicationException(String recordIdentifier) {
        super(recordIdentifier);
    }
}
