package pl.psnc.dei.exception;

public class DEIHttpException extends Exception {
    private int status;

    public DEIHttpException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "HTTP status: " + status + " Reason: " + getMessage();
    }
}
