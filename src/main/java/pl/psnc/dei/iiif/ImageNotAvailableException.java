package pl.psnc.dei.iiif;

public class ImageNotAvailableException extends RuntimeException {
	public ImageNotAvailableException() {
		super();
	}

	public ImageNotAvailableException(String message) {
		super(message);
	}

	public ImageNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
