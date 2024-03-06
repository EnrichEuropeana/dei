package pl.psnc.dei.iiif;

public class InvalidIIIFManifestException extends RuntimeException {
	public InvalidIIIFManifestException() {
		super();
	}

	public InvalidIIIFManifestException(String message) {
		super(message);
	}

	public InvalidIIIFManifestException(String message, Throwable cause) {
		super(message, cause);
	}
}
