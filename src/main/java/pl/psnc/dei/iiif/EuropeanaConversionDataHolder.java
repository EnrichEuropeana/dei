package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.util.IiifValidator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EuropeanaConversionDataHolder extends ConversionDataHolder {

	private static final Logger logger = LoggerFactory.getLogger(EuropeanaConversionDataHolder.class);

	public static final String KEY_GRAPH = "@graph";

	private static final String KEY_TYPE = "@type";

	private static final String TYPE_WEB_RESOURCE = "edm:WebResource";

	public static final String EDM_HAS_VIEW = "edm:hasView";

	public static final String EDM_IS_SHOWN_BY = "edm:isShownBy";

	public static final String KEY_ID = "@id";

	private static final String EDM_IS_NEXT_IN_SEQUENCE = "edm:isNextInSequence";

	private static final String FILE_URL_PREFIX = "file://";

	void createConversionDataHolder(String recordId, JsonObject aggregatorData, JsonObject record, JsonObject recordRaw) {
		Optional<String> isShownByMimeType = Optional.ofNullable(IiifValidator.getMimeTypeFromShort(detectType(aggregatorData.get(EDM_IS_SHOWN_BY).getAsObject().get(KEY_ID).getAsString().value(), record)));
		if (isShownByMimeType.filter(IiifValidator::isMimeTypeAllowed).isPresent()) {
			ConversionData isShownBy = new ConversionData();
			isShownBy.json = aggregatorData.get(EDM_IS_SHOWN_BY).getAsObject();
			isShownBy.mediaType = detectType(isShownBy.json.getAsObject().get(KEY_ID).getAsString().value(), record);
			fileObjects.add(isShownBy);
		}

		// get hasView objects and for each create ConversionData object
		if (aggregatorData.get(EDM_HAS_VIEW) != null) {
			final Map<String, Integer> urlPositions = prepareUrlPositions(recordRaw);
			if (aggregatorData.get(EDM_HAS_VIEW).isArray()) {
				fileObjects.addAll(aggregatorData.get(EDM_HAS_VIEW).getAsArray().stream()
						.filter(jsonValue -> isValidUrl(jsonValue.getAsObject().get(KEY_ID).getAsString().value()))
						.filter(jsonValue -> {
							Optional<String> mimeType = Optional.ofNullable(IiifValidator.getMimeTypeFromShort(detectType(jsonValue.getAsObject().get(KEY_ID).getAsString().value(), record)));
							return mimeType.filter(IiifValidator::isMimeTypeAllowed).isPresent();
						})
						.filter(jsonValue ->
								fileObjects.stream().noneMatch(conversionData -> conversionData.json.equals(jsonValue.getAsObject())))
						.map(e -> {
							ConversionData data = new ConversionData();
							data.json = e.getAsObject();
							data.mediaType = detectType(data.json.getAsObject().get(KEY_ID).getAsString().value(), record);
							return data;
						}).sorted(Comparator.comparing(conversionData -> urlPositions.get(conversionData.json.getAsObject().get(KEY_ID).getAsString().value())))
						.collect(Collectors.toList()));
			} else {
				JsonObject object = aggregatorData.get(EDM_HAS_VIEW).getAsObject();
				Optional<ConversionData> alreadyThere = fileObjects.stream().filter(conversionData -> conversionData.json.equals(object)).findFirst();
				if (!alreadyThere.isPresent()) {
					ConversionData data = new ConversionData();
					data.json = object;
					fileObjects.add(data);
					data.mediaType = detectType(data.json.getAsObject().get(KEY_ID).getAsString().value(), record);
				}
			}
		}

		reorderFileUrls(record);
		initFileUrls(recordId);
	}

	private Map<String, Integer> prepareUrlPositions(JsonObject recordRaw) {
		Map<String, Integer> urlPositions = new HashMap<>();
		JsonValue jsonValue = recordRaw.get("object").getAsObject().get("aggregations").getAsArray().get(0).getAsObject().get("hasView");
		if (jsonValue != null && jsonValue.isArray()) {
			JsonArray array = jsonValue.getAsArray();
			IntStream.range(0, array.size()).forEach(i -> {
				// Special case for records where JSON and JSONLD contain different data
				String url = array.get(i).getAsString().value();
				if (!isValidUrl(url) && url.startsWith("/")) {
					urlPositions.put(FILE_URL_PREFIX + url, i);
				} else {
					urlPositions.put(url, i);
				}
			});
		} else {
			assert jsonValue != null;
			// Special case for records where JSON and JSONLD contain different data
			String url = jsonValue.getAsString().toString();
			if (!isValidUrl(url) && url.startsWith("/")) {
				urlPositions.put(FILE_URL_PREFIX + url, 0);
			} else {
				urlPositions.put(url, 0);
			}
		}
		return urlPositions;
	}

	private void reorderFileUrls(JsonObject record) {
		List<JsonObject> webResources = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> anyTypeInArrayEquals(o.get(KEY_TYPE), TYPE_WEB_RESOURCE)
						&& fileObjects.stream().anyMatch(conversionData -> conversionData.json.getAsObject().get(KEY_ID).getAsString().value().equals(o.get(KEY_ID).getAsString().value())))
				.collect(Collectors.toList());
		List<ConversionData> ordered = new ArrayList<>();
		// find first
		Optional<JsonObject> element = webResources.stream().filter(jsonObject -> jsonObject.get(EDM_IS_NEXT_IN_SEQUENCE) == null).findFirst();
		while (element.isPresent()) {
			JsonObject url = element.get();
			ordered.add(fileObjects.stream().filter(conversionData -> conversionData.json.getAsObject().get(KEY_ID).getAsString().value().equals(url.get(KEY_ID).getAsString().value())).findFirst().get());
			element = webResources.stream().filter(jsonObject -> jsonObject.get(EDM_IS_NEXT_IN_SEQUENCE) != null && jsonObject.get(EDM_IS_NEXT_IN_SEQUENCE).getAsObject().get(KEY_ID).getAsString().value().equals(url.get(KEY_ID).getAsString().value())).findFirst();
		}
		if (!ordered.isEmpty() && ordered.size() == fileObjects.size()) {
			fileObjects.clear();
			fileObjects.addAll(ordered);
		}
	}

	private static boolean anyTypeInArrayEquals(JsonValue jsonValue, String typeToCheck) {
		if (jsonValue.isArray()) {
			return jsonValue.getAsArray().stream().anyMatch(value -> value.getAsString().value().equals(typeToCheck));
		}
		return jsonValue.getAsString().value().equals(typeToCheck);
	}

	private static Optional<String> extractMimeType(String id, JsonObject record) {
		return record.get(KEY_GRAPH).getAsArray()
				.stream()
				.filter(e -> e.getAsObject().get(KEY_ID).getAsString().value().equals(id))
				.map(e -> {
					if(e.getAsObject().get("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType") != null){
						return 	e.getAsObject().get("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType");
					}
					else if(e.getAsObject().get("ebucore:hasMimeType") != null){
						return e.getAsObject().get("ebucore:hasMimeType");
					}
					return null;
				})
				.filter(Objects::nonNull)
				.map(e -> e.getAsString().value())
				.findFirst();
	}

	public static String detectType(String id, JsonObject record) {
		Optional<String> typeRaw = extractMimeType(id, record);

		String type;

		if(!typeRaw.isPresent()){
			logger.info("Missing file type");
			int dotIndex = id.lastIndexOf('.');
			if(dotIndex != -1 && id.length() - dotIndex <= 5) {
				type = id.substring(dotIndex + 1);
			} else {
				type = null;
			}

		} else {
			String[] types = typeRaw.get().split("/");
			logger.info("Present file type");
			type = types[types.length-1];
		}
		return type;
	}

	EuropeanaConversionDataHolder(String recordId, JsonObject aggregatorData, JsonObject record, JsonObject recordRaw) {
		createConversionDataHolder(recordId, aggregatorData, record, recordRaw);
	}

	private boolean isValidUrl(String url) {
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	@Override
	void initFileUrls(String recordId) {
		for (ConversionData data : fileObjects) {
			String url = data.json.get(KEY_ID).getAsString().value();
			try {
				data.srcFileUrl = new URL(url);
			} catch (MalformedURLException e) {
				logger.error("Incorrect file URL for record: {}, url: {}", recordId, url, e);
			}
		}
	}
}
