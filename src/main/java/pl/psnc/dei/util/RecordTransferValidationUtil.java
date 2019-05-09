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

	private static String[] allowedTypes = {"image/jpeg", "image/tiff", "image/png", "application/pdf"};

	public static String getMimeType(JsonObject record) {
		Optional<JsonObject> first = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> o.get(KEY_TYPE).getAsString().value().equals(TYPE_WEB_RESOURCE)
						&& o.get(KEY_MIME_TYPE) != null)
				.findFirst();
		return first.map(jsonObject -> jsonObject.get(KEY_MIME_TYPE).getAsString().value()).orElse(null);
	}

	public static String checkIfTransferPossible(JsonObject record, String mimeType) {
		Optional<JsonObject> first = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> (o.get(KEY_TYPE).getAsString().value().equals(TYPE_SERVICE)
						&& o.get(KEY_CONFORMS_TO) != null
						&& o.get(KEY_CONFORMS_TO).getAsString().value().equals("http://iiif.io/api/image"))
						|| (o.get(KEY_TYPE).getAsString().value().equals(TYPE_AGGREGATION)
						&& o.get(KEY_IS_SHOWN_BY) != null
						&& o.get(KEY_IS_SHOWN_BY).getAsString().value().contains("iiif.europeana.eu")))
				.findFirst();
		if (first.isPresent()) {
			return "Can be transferred to TP";
		}
		if (Arrays.asList(allowedTypes).contains(mimeType)) {
			return "Can be converted and transferred to TP";
		}

		return "Cannot be transferred to TP";
	}
}
