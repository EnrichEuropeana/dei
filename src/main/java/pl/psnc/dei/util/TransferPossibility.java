package pl.psnc.dei.util;

/**
 * Possible results of check if record can be transferred to TP:
 * POSSIBLE - record already available via IIIF, can be transferred without conversion
 * REQUIRES_CONVERSION - record available in supported format, requires conversion to IIIF before transfer to TP
 * NOT_POSSIBLE - record not available in supported format, cannot be transferred to TP
 */
public enum TransferPossibility {
	POSSIBLE("Can be transferred to Transcription Platform", true),
	REQUIRES_CONVERSION("Can be converted and transferred to Transcription Platform", true),
	NOT_POSSIBLE("Cannot be transferred to Transcription Platform", false);

	String message;
	boolean transferPossible;

	TransferPossibility(String message, boolean transferPossible) {
		this.message = message;
		this.transferPossible = transferPossible;
	}

	public String getMessage() {
		return message;
	}

	public boolean isTransferPossible() {
		return transferPossible;
	}
}
