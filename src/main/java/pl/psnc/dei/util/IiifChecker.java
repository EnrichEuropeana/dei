package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import pl.psnc.dei.model.Aggregator;

import java.util.Optional;

public class IiifChecker {

	private static final String KEY_GRAPH = "@graph";
	private static final String KEY_TYPE = "@type";
	private static final String KEY_CONFORMS_TO = "conformsTo";
	private static final String KEY_DCTERMS_CONFORMS_TO = "dcterms:conformsTo";
	private static final String KEY_IS_SHOWN_BY = "isShownBy";

	private static final String TYPE_SERVICE = "svcs:Service";
	private static final String TYPE_AGGREGATION = "ore:Aggregation";


	/**
	 * Checks if given record is already available via IIIF
	 *
	 * @param record record json-ld object
	 * @param aggregator Aggregator
	 * @return true, if record is available via IIIF, false otherwise
	 */
	public static boolean checkIfIiif(JsonObject record, Aggregator aggregator) {
		switch (aggregator) {
			case EUROPEANA:
				return checkIfEuropeanaIiif(record);
			case DDB:
				return checkIfDDBIiif(record);
			default:
				throw new IllegalArgumentException("Unknown aggregator " + aggregator.getFullName());
		}
	}

	private static boolean checkIfEuropeanaIiif(JsonObject record) {
		Optional<JsonObject> iiifEntry = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> (anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_SERVICE)
						&& o.get(KEY_CONFORMS_TO) != null
						&& o.get(KEY_CONFORMS_TO).getAsString().value().equals("http://iiif.io/api/image"))
						|| (anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_AGGREGATION)
						&& o.get(KEY_IS_SHOWN_BY) != null
						&& o.get(KEY_IS_SHOWN_BY).getAsString().value().contains("iiif.europeana.eu"))
						|| ((anyTypeInArrayEquals(o.get(KEY_TYPE), "http://rdfs.org/sioc/services#Service") || (anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_SERVICE)))
						&& o.get(KEY_DCTERMS_CONFORMS_TO) != null
						&& o.get(KEY_DCTERMS_CONFORMS_TO).getAsObject().get("@id").getAsString().value().equals("http://iiif.io/api/image")))
				.findFirst();
		return iiifEntry.isPresent();
	}

	private static boolean anyTypeInArrayEquals(JsonValue jsonValue, String typeToCheck) {
		if (jsonValue.isArray()) {
			return jsonValue.getAsArray().stream().anyMatch(value -> value.getAsString().value().equals(typeToCheck));
		}
		return jsonValue.getAsString().value().equals(typeToCheck);
	}

	private static boolean checkIfDDBIiif(JsonObject record) {
		//we don't if we can check if record is available via iiif
		return false;
	}
}
