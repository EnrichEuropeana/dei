package pl.psnc.dei.util.ddb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DDBThumbnailUriCreator {

	public static String prepareThumbnailUri(String thumbnail) {
		if(thumbnail == null) {
			return "";
		}
		String currentProfile = getCurrentProfile();
		Properties properties = new Properties();
		InputStream inputStream = DDBThumbnailUriCreator.class.getClassLoader().getResourceAsStream("application-" + currentProfile + ".properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String apiKey = properties.getProperty("ddb.api.key");
		String binaryApiUri = properties.getProperty("ddb.binary.api.url");
		String apiUri = properties.getProperty("ddb.api.uri");

		return apiUri + binaryApiUri + thumbnail + "?oauth_consumer_key=" + apiKey;
	}

	private static String getCurrentProfile() {
		Properties properties = new Properties();
		InputStream inputStream = DDBThumbnailUriCreator.class.getClassLoader().getResourceAsStream("application.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties.getProperty("spring.profiles.active");
	}

}
