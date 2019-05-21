package pl.psnc.dei.exception;

public class NotFoundException extends Exception {

    public NotFoundException(String name) {
        super(name);
    }

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
