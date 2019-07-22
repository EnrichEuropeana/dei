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

	EuropeanaConversionDataHolder(String recordId, JsonObject aggregatorData, JsonObject record) {
		ConversionData isShownBy = new ConversionData();
		isShownBy.json = aggregatorData.get("edm:isShownBy").getAsObject();
		fileObjects.add(isShownBy);
		String mainFileUrl = isShownBy.json.get("@id").getAsString().value();

		Optional<String[]> typeRaw = record.get("@graph").getAsArray()
				.stream()
				.filter(e -> e.getAsObject().get("@id").getAsString().value().equals(mainFileUrl))
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
			type = mainFileUrl.substring(mainFileUrl.lastIndexOf('.'));
		} else {
			String[] types = typeRaw.get();
			logger.info("Present file type");
			type = types[types.length-1];
		}


		if(aggregatorData.get("edm:hasView") != null) {
			if (aggregatorData.get("edm:hasView").isArray()) {
				fileObjects.addAll(aggregatorData.get("edm:hasView").getAsArray().stream()
						.map(e -> {
							ConversionData data = new ConversionData();
							data.json = e.getAsObject();
							data.mediaType = type;
							return data;
						})
						.collect(Collectors.toList()));
			} else {
					ConversionData data = new ConversionData();
					data.json = aggregatorData.get("edm:hasView").getAsObject();
					fileObjects.add(data);
					data.mediaType = type;
			}
		}

		initFileUrls(recordId);

		for (ConversionData data : fileObjects) {
			data.mediaType = type;
		}
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
