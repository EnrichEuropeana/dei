package pl.psnc.dei.exception;

public class TaskCreationException extends Exception {

	public TaskCreationException() {
		super();
	}

	public TaskCreationException(String message) {
		super(message);
	}

	public TaskCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
