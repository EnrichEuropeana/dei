package pl.psnc.dei.util;

import lombok.experimental.UtilityClass;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import pl.psnc.dei.iiif.InvalidIIIFManifestException;
import pl.psnc.dei.model.Aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class IiifChecker {

    private static final String KEY_GRAPH = "@graph";
    private static final String KEY_TYPE = "@type";
    private static final String KEY_CONFORMS_TO = "conformsTo";
    private static final String KEY_DCTERMS_CONFORMS_TO = "dcterms:conformsTo";
    private static final String KEY_IS_SHOWN_BY = "isShownBy";
    private static final String KEY_HAS_SERVICE = "svcs:has_service";
    private static final String KEY_ID = "@id";
    private static final String KEY_DCTERMS_IS_REFERENCED_BY = "dcterms:isReferencedBy";

    private static final String TYPE_SERVICE = "svcs:Service";
    private static final String TYPE_AGGREGATION = "ore:Aggregation";
    private static final String TYPE_WEB_RESOURCE = "edm:WebResource";

    private static final Pattern CONTEXT_PATTERN = Pattern.compile(
            "http://iiif\\.io/api/presentation/(2|2\\.0|2\\.1|3|3\\.0)/context\\.json");

    /**
     * Checks if given record is already available via IIIF
     *
     * @param record     record json-ld object
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
                        || ((anyTypeInArrayEquals(o.get(KEY_TYPE), "http://rdfs.org/sioc/services#Service") ||
                        (anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_SERVICE)))
                        && o.get(KEY_DCTERMS_CONFORMS_TO) != null
                        && o.get(KEY_DCTERMS_CONFORMS_TO).getAsObject().get("@id").getAsString().value()
                        .equals("http://iiif.io/api/image")))
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

    public static String extractIIIFManifestURL(JsonObject record, Aggregator aggregator) throws
            InvalidIIIFManifestException {
        if (checkIfIiif(record, aggregator)) {
            return extractIIIFManifestURL(record);
        }
        return Optional.ofNullable(extractLocalIIIFManifestURL(record))
                .orElseThrow(() -> new InvalidIIIFManifestException("No iiif manifest found in the record."));
    }

    private static String extractLocalIIIFManifestURL(JsonObject record) {
        if (record.get("iiif_url") != null) {
            return record.get("iiif_url").getAsString().value();
        }
        return null;
    }

    private static String extractIIIFManifestURL(JsonObject record) {
        AtomicReference<String> url = new AtomicReference<>();
        record.get(KEY_GRAPH).getAsArray().stream()
                .map(JsonValue::getAsObject)
                .filter(o -> (anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_SERVICE)
                        && o.get(KEY_CONFORMS_TO) != null
                        && o.get(KEY_CONFORMS_TO).getAsString().value().equals("http://iiif.io/api/image"))
                        || (anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_AGGREGATION)
                        && o.get(KEY_IS_SHOWN_BY) != null
                        && o.get(KEY_IS_SHOWN_BY).getAsString().value().contains("iiif.europeana.eu"))
                        || ((anyTypeInArrayEquals(o.get(KEY_TYPE), "http://rdfs.org/sioc/services#Service") ||
                        (anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_SERVICE)))
                        && o.get(KEY_DCTERMS_CONFORMS_TO) != null
                        && o.get(KEY_DCTERMS_CONFORMS_TO).getAsObject().get("@id").getAsString().value()
                        .equals("http://iiif.io/api/image")))
                .findFirst().flatMap(jsonObject -> record.get(KEY_GRAPH).getAsArray().stream()
                        .map(JsonValue::getAsObject)
                        .filter(o -> anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_WEB_RESOURCE)
                                && o.get(KEY_HAS_SERVICE) != null
                                && o.get(KEY_HAS_SERVICE).getAsObject().get(KEY_ID).getAsString().value()
                                .equals(jsonObject.get(KEY_ID).getAsString().value()))
                        .findFirst())
                .ifPresent(iiifResource -> {
                    if (iiifResource.get(KEY_DCTERMS_IS_REFERENCED_BY) != null) {
                        url.set(iiifResource.get(KEY_DCTERMS_IS_REFERENCED_BY).getAsObject().get(KEY_ID).getAsString()
                                .value());
                    }
                });
        return url.get();
    }

    public static String extractVersion(String iiifManifest) {
        JsonObject jsonObject = JSON.parse(iiifManifest.replace('\u00A0',' '));
        String context = jsonObject.get("@context").getAsString().value();
        Matcher matcher = CONTEXT_PATTERN.matcher(context);
        if (matcher.matches()) {
            return getVersionForValidation(matcher.group(1));
        }
        throw new InvalidIIIFManifestException(
                String.format("Presentation API version could not be extracted from manifest @contex element %s.",
                        context));
    }

    private String getVersionForValidation(String extractedVersion) {
        if (extractedVersion.equals("2")) {
            return "2.1";
        }
        if (extractedVersion.equals("3")) {
            return "3.0";
        }
        return extractedVersion;
    }

    public static List<String> extractImages(String iiifManifest) {
        List<String> extractedImages = new ArrayList<>();
        JsonObject jsonObject = JSON.parse(iiifManifest.replace('\u00A0',' '));
        JsonArray canvas = jsonObject.get("sequences").getAsArray().get(0).getAsObject().get("canvases").getAsArray();
        canvas.stream().iterator().forEachRemaining(canva -> {
            JsonArray images = canva.getAsObject().get("images").getAsArray();
            images.stream().iterator().forEachRemaining(image -> {
                JsonObject service = image.getAsObject().get("resource").getAsObject().get("service").getAsObject();
                if (service.isArray()) {
                    extractedImages.add(
                            getInfoJson(service.getAsArray().get(0).getAsObject().get("@id").getAsString().value()));
                } else {
                    extractedImages.add(getInfoJson(service.getAsObject().get("@id").getAsString().value()));
                }
            });
        });
        return extractedImages;
    }

    private static String getInfoJson(String value) {
        return value + "/info.json";
    }
}
