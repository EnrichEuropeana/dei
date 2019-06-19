package pl.psnc.dei.util.ddb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DDBUriCreator {

	private static final Logger logger = LoggerFactory.getLogger(DDBUriCreator.class);

	public static String prepareThumbnailUri(String thumbnail) {
		if(thumbnail == null) {
			return "";
		}
		String currentProfile = getCurrentProfile();
		Properties properties = new Properties();
		InputStream inputStream = DDBUriCreator.class.getClassLoader().getResourceAsStream("application-" + currentProfile + ".properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.warn("Cannot load properties for profile {}", currentProfile,  e);
		}

		String apiKey = properties.getProperty("ddb.api.key");
		String binaryApiUri = properties.getProperty("ddb.binary.api.url");
		String apiUri = properties.getProperty("ddb.api.url");

		return apiUri + binaryApiUri + thumbnail + "?oauth_consumer_key=" + apiKey;
	}

	public static String prepareSourceObjectUri(String recordId) {
		if (recordId == null || recordId.isEmpty()) {
			return null;
		}

		Properties properties = getBaseProperties();
		String itemUrl = properties.getProperty("ddb.item.url");
		return itemUrl + recordId;
	}

	private static String getCurrentProfile() {
		Properties properties = getBaseProperties();
		return properties.getProperty("spring.profiles.active");
	}

	private static Properties getBaseProperties() {
		Properties properties = new Properties();
		InputStream inputStream = DDBUriCreator.class.getClassLoader().getResourceAsStream("application.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.warn("Cannot load application properties", e);
		}
		return properties;
	}
}
