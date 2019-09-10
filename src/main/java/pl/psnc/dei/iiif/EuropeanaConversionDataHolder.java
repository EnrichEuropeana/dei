package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class EuropeanaConversionDataHolder extends ConversionDataHolder {

	private static final Logger logger = LoggerFactory.getLogger(EuropeanaConversionDataHolder.class);

	void createConversionDataHolder(String recordId, JsonObject aggregatorData, JsonObject record) {
		ConversionData isShownBy = new ConversionData();
		isShownBy.json = aggregatorData.get("edm:isShownBy").getAsObject();
		isShownBy.mediaType = detectType(isShownBy.json.getAsObject().get("@id").getAsString().value(), record);
		fileObjects.add(isShownBy);

		// get hasView objects and for each create ConversionData object
		if (aggregatorData.get("edm:hasView") != null) {
			if (aggregatorData.get("edm:hasView").isArray()) {
				fileObjects.addAll(aggregatorData.get("edm:hasView").getAsArray().stream()
						.filter(jsonValue -> fileObjects.stream().noneMatch(conversionData -> conversionData.json.equals(jsonValue.getAsObject())))
						.map(e -> {
							ConversionData data = new ConversionData();
							data.json = e.getAsObject();
							data.mediaType = detectType(data.json.getAsObject().get("@id").getAsString().value(), record);
							return data;
						})
						.collect(Collectors.toList()));
			} else {
				JsonObject object = aggregatorData.get("edm:hasView").getAsObject();
				Optional<ConversionData> alreadyThere = fileObjects.stream().filter(conversionData -> conversionData.json.equals(object)).findFirst();
				if (!alreadyThere.isPresent()) {
					ConversionData data = new ConversionData();
					data.json = object;
					fileObjects.add(data);
					data.mediaType = detectType(data.json.getAsObject().get("@id").getAsString().value(), record);
				}
			}
		}

		initFileUrls(recordId);
	}

	String detectType(String id, JsonObject record) {
		Optional<String[]> typeRaw = record.get("@graph").getAsArray()
				.stream()
				.filter(e -> e.getAsObject().get("@id").getAsString().value().equals(id))
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
				.map(e -> e.getAsString().value().split("/"))
				.findFirst();

		String type;

		if(!typeRaw.isPresent()){
			logger.info("Missing file type");
			int dotIndex = id.lastIndexOf('.');
			if(dotIndex != -1) {
				type = id.substring(dotIndex);
			} else {
				type = null;
			}

		} else {
			String[] types = typeRaw.get();
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
			String url = data.json.get("@id").getAsString().value();
			try {
				data.srcFileUrl = new URL(url);
			} catch (MalformedURLException e) {
				logger.error("Incorrect file URL for record: {}, url: {}", recordId, url, e);
			}
		}
	}
}
