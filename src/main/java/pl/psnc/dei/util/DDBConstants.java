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

	static {
		FACET_LABELS = new HashMap<>();
		FACET_LABELS.put("affiliate_fct_role", "Person/organisation");
		FACET_LABELS.put("type_fct", "Media type");
		FACET_LABELS.put("place_fct", "Place");
		FACET_LABELS.put("keywords_fct", "Keyword");
		FACET_LABELS.put("language_fct", "Language");
		FACET_LABELS.put("provider_fct", "Data provider");
		FACET_LABELS.put("sector_fct", "Sector");
		FACET_LABELS.put("license_group", "Usability");
	}

	private DDBConstants() {
	}
}
