package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public class RecordTransferValidationUtil {

	private static final String KEY_GRAPH = "@graph";
	private static final String KEY_TYPE = "@type";
	private static final String KEY_MIME_TYPE = "ebucore:hasMimeType";
	private static final String KEY_CONFORMS_TO = "conformsTo";
	private static final String KEY_IS_SHOWN_BY = "isShownBy";

	private static final String TYPE_WEB_RESOURCE = "edm:WebResource";
	private static final String TYPE_SERVICE = "svcs:Service";
	private static final String TYPE_AGGREGATION = "ore:Aggregation";

	private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/tiff", "image/png", "application/pdf"};

	/**
	 * Get mimeType for given record
	 *
	 * @param record record json-ld object
	 * @return record's mimeType
	 */
	public static String getMimeType(JsonObject record) {
		Optional<JsonObject> mimeTypeEntry = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> o.get(KEY_TYPE).getAsString().value().equals(TYPE_WEB_RESOURCE)
						&& o.get(KEY_MIME_TYPE) != null)
				.findFirst();
		return mimeTypeEntry.map(jsonObject -> jsonObject.get(KEY_MIME_TYPE).getAsString().value()).orElse(null);
	}

	/**
	 * Checks if given record can be converted and/or transferred to TP
	 *
	 * @param record   record json-ld object
	 * @param mimeType record's mimeType
	 * @return {@link TransferPossibility}
	 */
	public static TransferPossibility checkIfTransferPossible(JsonObject record, String mimeType) {
		if (checkIfIiif(record)) {
			return TransferPossibility.POSSIBLE;
		}
		if (Arrays.asList(ALLOWED_TYPES).contains(mimeType)) {
			return TransferPossibility.REQUIRES_CONVERSION;
		}
		return TransferPossibility.NOT_POSSIBLE;
	}

	/**
	 * Checks if given record is already available via IIIF
	 *
	 * @param record record json-ld object
	 * @return true, if record is available via IIIF, false otherwise
	 */
	public static boolean checkIfIiif(JsonObject record) {
		Optional<JsonObject> iiifEntry = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> (o.get(KEY_TYPE).getAsString().value().equals(TYPE_SERVICE)
						&& o.get(KEY_CONFORMS_TO) != null
						&& o.get(KEY_CONFORMS_TO).getAsString().value().equals("http://iiif.io/api/image"))
						|| (o.get(KEY_TYPE).getAsString().value().equals(TYPE_AGGREGATION)
						&& o.get(KEY_IS_SHOWN_BY) != null
						&& o.get(KEY_IS_SHOWN_BY).getAsString().value().contains("iiif.europeana.eu")))
				.findFirst();
		return iiifEntry.isPresent();
	}

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
}
