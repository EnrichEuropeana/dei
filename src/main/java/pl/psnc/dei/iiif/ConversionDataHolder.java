package pl.psnc.dei.iiif;

import org.apache.jena.atlas.json.JsonObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class ConversionDataHolder {

	List<ConversionData> fileObjects = new ArrayList<>();

	abstract void initFileUrls(String recordId);

	public static class ConversionData {
		JsonObject json;
		URL srcFileUrl;
		List<File> outFile = new ArrayList<>();
		File srcFile;
		List<String> imagePath = new ArrayList<>();
		String mediaType;
		List<Dimension> dimensions = new ArrayList<>();
	}
}
