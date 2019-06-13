package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.model.Aggregator;

import java.util.ArrayList;
import java.util.List;

public class RecordTransferValidator {

	private static final List<String> ALLOWED_TYPES = new ArrayList<>();

	static {
		ALLOWED_TYPES.add("image/jpeg");
		ALLOWED_TYPES.add("image/tiff");
		ALLOWED_TYPES.add("image/png");
		ALLOWED_TYPES.add("application/pdf");
	}

	/**
	 * Checks if given record can be converted and/or transferred to TP
	 *
	 * @param record   record json-ld object
	 * @param mimeType record's mimeType
	 * @return {@link IiifAvailability}
	 */
	public static IiifAvailability checkIfIiifAvailable(Aggregator aggregator, JsonObject record, String mimeType) {
		if (record == null) {
			return IiifAvailability.DATA_UNAVAILABLE;
		}
		if (IiifChecker.checkIfIiif(record, aggregator)) {
			return IiifAvailability.AVAILABLE;
		}
		if (ALLOWED_TYPES.contains(mimeType)) {
			return IiifAvailability.CONVERSION_POSSIBLE;
		}
		return IiifAvailability.CONVERSION_IMPOSSIBLE;
	}
}
