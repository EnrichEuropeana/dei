package pl.psnc.dei.iiif;

public class ConversionException extends Exception {
	public ConversionException() {
		super();
	}

	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}
}
