package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EuropeanaRecordTransferValidationUtil {

	private static final String KEY_GRAPH = "@graph";
	private static final String KEY_TYPE = "@type";
	private static final String KEY_MIME_TYPE = "hasMimeType";
	private static final String KEY_CONFORMS_TO = "conformsTo";
	private static final String KEY_DCTERMS_CONFORMS_TO = "dcterms:conformsTo";
	private static final String KEY_IS_SHOWN_BY = "isShownBy";

	private static final String TYPE_WEB_RESOURCE = "edm:WebResource";
	private static final String TYPE_SERVICE = "svcs:Service";
	private static final String TYPE_AGGREGATION = "ore:Aggregation";

	private static final List<String> ALLOWED_TYPES = new ArrayList<>();

	static {
		ALLOWED_TYPES.add("image/jpeg");
		ALLOWED_TYPES.add("image/tiff");
		ALLOWED_TYPES.add("image/png");
		ALLOWED_TYPES.add("application/pdf");
	}

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
						&& o.keySet().stream().anyMatch(k -> k.contains(KEY_MIME_TYPE)))
				.findFirst();
		if (mimeTypeEntry.isPresent()) {
			JsonObject object = mimeTypeEntry.get();
			return (object.get("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#" + KEY_MIME_TYPE) != null ?
					object.get("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#" + KEY_MIME_TYPE) :
					(object.get("ebucore:" + KEY_MIME_TYPE) != null ? object.get("ebucore:" + KEY_MIME_TYPE) :
							object.get(KEY_MIME_TYPE))).getAsString().value();
		}
		return null;
	}

	/**
	 * Checks if given record can be converted and/or transferred to TP
	 *
	 * @param record   record json-ld object
	 * @param mimeType record's mimeType
	 * @return {@link IiifAvailability}
	 */
	public static IiifAvailability checkIfIiifAvailable(JsonObject record, String mimeType) {
		if (record == null) {
			return IiifAvailability.DATA_UNAVAILABLE;
		}
		if (checkIfIiif(record)) {
			return IiifAvailability.AVAILABLE;
		}
		if (ALLOWED_TYPES.contains(mimeType)) {
			return IiifAvailability.CONVERSION_POSSIBLE;
		}
		return IiifAvailability.CONVERSION_IMPOSSIBLE;
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
						&& o.get(KEY_IS_SHOWN_BY).getAsString().value().contains("iiif.europeana.eu"))
						|| (o.get(KEY_TYPE).getAsString().value().equals("http://rdfs.org/sioc/services#Service")
						&& o.get(KEY_DCTERMS_CONFORMS_TO) != null
						&& o.get(KEY_DCTERMS_CONFORMS_TO).getAsObject().get("@id").getAsString().value().equals("http://iiif.io/api/image")))
				.findFirst();
		return iiifEntry.isPresent();
	}
}
