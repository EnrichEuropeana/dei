package pl.psnc.dei.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DDBConstants {

	//DDB api request's parameters names
	public static final String DDB_API_KEY_PARAM_NAME = "oauth_consumer_key";
	public static final String QUERY_PARAM_NAME = "query";
	public static final String ROWS_PARAM_NAME = "rows";
	public static final String OFFSET_PARAM_NAME = "offset";
	public static final String FACET_PARAM_NAME = "facet";

	public static final List<String> FACET_NAMES = Arrays.asList("affiliate_fct_role", "type_fct", "place_fct",
			"keywords_fct", "language_fct", "provider_fct", "sector_fct", "license_group");

	//DDB labels for facet fields
	public static final Map<String, String> FACET_LABELS;

	public static final Map<String, String> FACET_VALUES_NAMES;
	private static final Map<String, String> MEDIA_TYPES_NAMES;
	private static final Map<String, String> SECTORS_NAMES;
	private static final Map<String, String> RIGHTS_NAMES;

	static {
		FACET_LABELS = new HashMap<>();
		FACET_LABELS.put("affiliate_fct_role", "Person/organisation");
		FACET_LABELS.put("type_fct", "Media type");
		FACET_LABELS.put("place_fct", "Location");
		FACET_LABELS.put("keywords_fct", "Keyword");
		FACET_LABELS.put("language_fct", "Language");
		FACET_LABELS.put("provider_fct", "Data provider");
		FACET_LABELS.put("sector_fct", "Sector");
		FACET_LABELS.put("license_group", "Usability");

		MEDIA_TYPES_NAMES = new HashMap<>();
		MEDIA_TYPES_NAMES.put("mediatype_001", "Audio");
		MEDIA_TYPES_NAMES.put("mediatype_002", "Image");
		MEDIA_TYPES_NAMES.put("mediatype_003", "Text");
		MEDIA_TYPES_NAMES.put("mediatype_004", "Full-Text");
		MEDIA_TYPES_NAMES.put("mediatype_005", "Video");
		MEDIA_TYPES_NAMES.put("mediatype_006", "Others");
		MEDIA_TYPES_NAMES.put("mediatype_007", "No media type");
		MEDIA_TYPES_NAMES.put("mediatype_008", "Institution");

		SECTORS_NAMES = new HashMap<>();
		SECTORS_NAMES.put("sec_01", "Archive");
		SECTORS_NAMES.put("sec_02", "Library");
		SECTORS_NAMES.put("sec_03", "Monument protection");
		SECTORS_NAMES.put("sec_04", "Research");
		SECTORS_NAMES.put("sec_05", "Media");
		SECTORS_NAMES.put("sec_06", "Museum");
		SECTORS_NAMES.put("sec_07", "Other");

		RIGHTS_NAMES = new HashMap<>();
		RIGHTS_NAMES.put("rights_001", "Without restrictions");
		RIGHTS_NAMES.put("rights_002", "With restrictions");
		RIGHTS_NAMES.put("rights_003", "On request");

		FACET_VALUES_NAMES = new HashMap<>();
		FACET_VALUES_NAMES.putAll(MEDIA_TYPES_NAMES);
		FACET_VALUES_NAMES.putAll(SECTORS_NAMES);
		FACET_VALUES_NAMES.putAll(RIGHTS_NAMES);
	}

	private DDBConstants() {
	}
}
