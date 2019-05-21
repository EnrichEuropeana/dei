package pl.psnc.dei.iiif;

public class ConversionImpossibleException extends ConversionException {

	public ConversionImpossibleException() {
		super();
	}

	public ConversionImpossibleException(String message) {
		super(message);
	}

	public ConversionImpossibleException(String message, Throwable cause) {
		super(message, cause);
	}
}
