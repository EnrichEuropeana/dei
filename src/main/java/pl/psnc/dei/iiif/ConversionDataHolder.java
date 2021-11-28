package pl.psnc.dei.iiif;

import lombok.Getter;
import lombok.Setter;
import org.apache.jena.atlas.json.JsonObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class ConversionDataHolder {

	// contains information about single object to convert into IIIF
	List<ConversionData> fileObjects = new ArrayList<>();

	abstract void initFileUrls(String recordId);

	@Getter
	@Setter
	public static class ConversionData {
		JsonObject json;
		// file to download and convert
		URL srcFileUrl;
		// generated objects
		List<File> outFile = new ArrayList<>();
		// file to which download
		File srcFile;
		// paths to generated object
		List<String> imagePath = new ArrayList<>();
		// file extension
		String mediaType;
		// dimensions of saved object
		List<Dimension> dimensions = new ArrayList<>();
	}
}
