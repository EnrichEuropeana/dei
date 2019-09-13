package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class EuropeanaConversionDataHolder extends ConversionDataHolder {

	private static final Logger logger = LoggerFactory.getLogger(EuropeanaConversionDataHolder.class);

	public static final String KEY_GRAPH = "@graph";

	private static final String KEY_TYPE = "@type";

	private static final String TYPE_WEB_RESOURCE = "edm:WebResource";

	public static final String EDM_HAS_VIEW = "edm:hasView";

	public static final String EDM_IS_SHOWN_BY = "edm:isShownBy";

	public static final String KEY_ID = "@id";

	private static final String EDM_IS_NEXT_IN_SEQUENCE = "edm:isNextInSequence";

	void createConversionDataHolder(String recordId, JsonObject aggregatorData, JsonObject record) {
		ConversionData isShownBy = new ConversionData();
		isShownBy.json = aggregatorData.get(EDM_IS_SHOWN_BY).getAsObject();
		isShownBy.mediaType = detectType(isShownBy.json.getAsObject().get(KEY_ID).getAsString().value(), record);
		fileObjects.add(isShownBy);

		// get hasView objects and for each create ConversionData object
		if (aggregatorData.get(EDM_HAS_VIEW) != null) {
			if (aggregatorData.get(EDM_HAS_VIEW).isArray()) {
				fileObjects.addAll(aggregatorData.get(EDM_HAS_VIEW).getAsArray().stream()
						.filter(jsonValue -> fileObjects.stream().noneMatch(conversionData -> conversionData.json.equals(jsonValue.getAsObject())))
						.map(e -> {
							ConversionData data = new ConversionData();
							data.json = e.getAsObject();
							data.mediaType = detectType(data.json.getAsObject().get(KEY_ID).getAsString().value(), record);
							return data;
						})
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

	private void reorderFileUrls(JsonObject record) {
		List<JsonObject> webResources = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> o.get(KEY_TYPE).getAsString().value().equals(TYPE_WEB_RESOURCE)
						&& fileObjects.stream().anyMatch(conversionData -> conversionData.json.getAsObject().get(KEY_ID).getAsString().value().equals(o.get(KEY_ID).getAsString().value())))
				.collect(Collectors.toList());
		if (webResources != null) {
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
	}

	public static Optional<String> extractMimeType(String id, JsonObject record) {
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

	private String detectType(String id, JsonObject record) {
		Optional<String> typeRaw = extractMimeType(id, record);

		String type;

		if(!typeRaw.isPresent()){
			logger.info("Missing file type");
			int dotIndex = id.lastIndexOf('.');
			if(dotIndex != -1) {
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

	EuropeanaConversionDataHolder(String recordId, JsonObject aggregatorData, JsonObject record) {
		createConversionDataHolder(recordId, aggregatorData, record);
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
