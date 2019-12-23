package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.model.Aggregator;

import java.util.ArrayList;
import java.util.List;

public class IiifValidator {

	private static final List<String> ALLOWED_TYPES = new ArrayList<>();

	static {
		ALLOWED_TYPES.add("image/jpeg");
		ALLOWED_TYPES.add("image/tiff");
		ALLOWED_TYPES.add("image/png");
		ALLOWED_TYPES.add("application/pdf");
	}

	/**
	 * Checks if IIF is available or conversion is possible for given record.
	 *
	 * @param aggregator aggregator object
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
			//todo remove below if statement when ddb binaries endpoint become available
			if (aggregator == Aggregator.DDB) {
				return IiifAvailability.CONVERSION_IMPOSSIBLE;
			}
			return IiifAvailability.CONVERSION_POSSIBLE;
		}
		return IiifAvailability.CONVERSION_IMPOSSIBLE;
	}

	public static boolean isMimeTypeAllowed(String mimeType) {
		return ALLOWED_TYPES.contains(mimeType);
	}
}
