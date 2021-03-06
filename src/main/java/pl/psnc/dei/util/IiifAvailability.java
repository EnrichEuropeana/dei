package pl.psnc.dei.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Possible results of check if record is available via IIIF, also determines if record can be transferred to TP:
 * AVAILABLE - record already available via IIIF, can be transferred without conversion
 * CONVERSION_POSSIBLE - record available in supported format, requires conversion to IIIF before transfer to TP
 * CONVERSION_IMPOSSIBLE - record not available in supported format, cannot be transferred to TP
 * DATA_UNAVAILABLE - cannot retrieve record data, cannot be transferred to TP
 */
@Getter
@AllArgsConstructor
public enum IiifAvailability {
	AVAILABLE("Available", true),
	CONVERSION_POSSIBLE("Conversion possible", true),
	CONVERSION_IMPOSSIBLE("Conversion impossible", false),
	DATA_UNAVAILABLE("Data unavailable", false);

	String message;
	boolean transferPossible;

	public boolean isTransferPossible() {
		return transferPossible;
	}
}
