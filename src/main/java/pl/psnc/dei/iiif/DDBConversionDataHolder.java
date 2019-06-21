package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.util.ddb.DDBUriCreator;

import java.net.MalformedURLException;
import java.net.URL;

public class DDBConversionDataHolder extends ConversionDataHolder {

	private static final Logger logger = LoggerFactory.getLogger(DDBConversionDataHolder.class);

	public DDBConversionDataHolder(String recordId, JsonObject aggregatorData) {

		Object binary = aggregatorData.get("binary");
		if (binary instanceof JsonArray) {
			JsonArray binaries = (JsonArray) binary;
			binaries.stream()
					.map(JsonValue::getAsObject)
					.forEach(o -> {
						ConversionData data = new ConversionData();
						data.json = o.getAsObject();
						fileObjects.add(data);
					});
		} else {
			JsonObject singleBinary = (JsonObject) binary;
			ConversionData data = new ConversionData();
			data.json = singleBinary.getAsObject();
			fileObjects.add(data);
		}
		initFileUrls(recordId);
	}

	@Override
	void initFileUrls(String recordId) {
		for (ConversionData data : fileObjects) {
			String resourceId = data.json.get("@ref").getAsString().value();
			String url = DDBUriCreator.prepareObjectUriForConversion(resourceId);
			try {
				data.srcFileUrl = new URL(url);
			} catch (MalformedURLException e) {
				logger.error("Incorrect file URL for record: {}, url: {}", recordId, url, e);
			}
		}
	}
}
