package pl.psnc.dei.util;

import java.util.HashMap;
import java.util.Map;

import static pl.psnc.dei.ui.pages.SearchPage.ONLY_IIIF_PARAM_NAME;

public final class EuropeanaConstants {

	private static final String MEDIA = "MEDIA";
	private static final String REUSABILITY = "REUSABILITY";

	//Europeana persistent identifier URL
	public static final String EUROPEANA_ITEM_URL = "https://data.europeana.eu/item";

	//Europeana api request's parameters names
	public static final String API_KEY_PARAM_NAME = "wskey";
	public static final String QUERY_PARAM_NAME = "query";
	public static final String QF_PARAM_NAME = "qf";
	public static final String CURSOR_PARAM_NAME = "cursor";
	public static final String ROWS_PARAM_NAME = "rows";

	//Europeana default cursor
	public static final String FIRST_CURSOR = "*";

	//Europeana labels for facet fields
	public static final Map<String, String> FACET_LABELS;

	//Collection of fixed request parameters for Europeana search
	public static final String[] FIXED_API_PARAMS = {QUERY_PARAM_NAME, QF_PARAM_NAME, CURSOR_PARAM_NAME, ONLY_IIIF_PARAM_NAME};

	//Collection of facets for new search request
	public static final Map<String, String> EUROPEANA_DEFAULT_FACETS = new HashMap<>();

	//Collection of facets that are separate parameters in Europeana api request
	public static final String[] PARAM_FACETS = {"COLOURPALETTE", "LANDINGPAGE", MEDIA, REUSABILITY, "TEXT_FULLTEXT", "THUMBNAIL"};

	static {
		FACET_LABELS = new HashMap<>();
		FACET_LABELS.put("YEAR", "Year");
		FACET_LABELS.put("RIGHTS", "Rights");
		FACET_LABELS.put("DATA_PROVIDER", "Data provider");
		FACET_LABELS.put("PROVIDER", "Provider");
		FACET_LABELS.put("COLOURPALETTE", "Colour palette");
		FACET_LABELS.put("COUNTRY", "Country");
		FACET_LABELS.put("LANGUAGE", "Language");
		FACET_LABELS.put("MIME_TYPE", "Mime type");
		FACET_LABELS.put("TYPE", "Type");
		FACET_LABELS.put("IMAGE_SIZE", "Image size");
		FACET_LABELS.put("SOUND_DURATION", "Sound duration");
		FACET_LABELS.put(REUSABILITY, "Reusability");
		FACET_LABELS.put("VIDEO_DURATION", "Video duration");
		FACET_LABELS.put("TEXT_FULLTEXT", "Has fulltext");
		FACET_LABELS.put("LANDINGPAGE", "Landing page");
		FACET_LABELS.put(MEDIA, "Media");
		FACET_LABELS.put("THUMBNAIL", "Thumbnail");
		FACET_LABELS.put("UGC", "UGC");
		FACET_LABELS.put("IMAGE_ASPECTRATIO", "Image aspect ratio");
		FACET_LABELS.put("IMAGE_COLOUR", "Image colour");
		FACET_LABELS.put("VIDEO_HD", "Video HD");
		FACET_LABELS.put("SOUND_HQ", "Sound HQ");
	}

	private EuropeanaConstants() {
	}
}
