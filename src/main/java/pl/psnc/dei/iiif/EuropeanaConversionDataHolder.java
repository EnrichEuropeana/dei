package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class EuropeanaConversionDataHolder extends ConversionDataHolder {

	private static final Logger logger = LoggerFactory.getLogger(EuropeanaConversionDataHolder.class);

	EuropeanaConversionDataHolder(String recordId, JsonObject aggregatorData) {
		ConversionData isShownBy = new ConversionData();
		isShownBy.json = aggregatorData.get("edm:isShownBy").getAsObject();
		fileObjects.add(isShownBy);
		String mainFileUrl = isShownBy.json.get("@id").getAsString().value();
		String mainFileFormat = mainFileUrl.substring(mainFileUrl.lastIndexOf('.'));

		if (aggregatorData.get("edm:hasView") != null)
			fileObjects.addAll(aggregatorData.get("edm:hasView").getAsArray().stream()
					.filter(e -> e.getAsObject().get("@id").getAsString().value().endsWith(mainFileFormat))
					.map(e -> {
						ConversionData data = new ConversionData();
						data.json = e.getAsObject();
						return data;
					})
					.collect(Collectors.toList()));

		initFileUrls(recordId);
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
