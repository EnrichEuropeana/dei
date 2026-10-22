package pl.psnc.dei.exception;

public class DownloadFileException extends Exception {

    private String url;
    public static final String MESSAGE_FORMAT = "Cannot download file: %s";

    public DownloadFileException() {
        super();
    }

    public DownloadFileException(String url) {
        super(String.format(MESSAGE_FORMAT, url));

    }

    public DownloadFileException(String url, Throwable cause) {
        super(String.format(MESSAGE_FORMAT, url), cause);
    }
}
