package pl.psnc.dei.util;

import java.util.Arrays;
import java.util.List;

public final class DDBConstants {

	//DDB api request's parameters names
	public static final String DDB_API_KEY_PARAM_NAME = "oauth_consumer_key";
	public static final String QUERY_PARAM_NAME = "query";
	public static final String ROWS_PARAM_NAME = "rows";
	public static final String OFFSET_PARAM_NAME = "offset";
	public static final String FACET_PARAM_NAME = "facet";

	public static final List<String> FACET_NAMES = Arrays.asList("affiliate_fct", "type_fct", "time_fct", "place_fct", "keywords_fct", "language_fct");

	private DDBConstants() {
	}
}
