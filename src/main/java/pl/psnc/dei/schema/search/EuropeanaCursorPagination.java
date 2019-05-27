package pl.psnc.dei.schema.search;

import java.util.HashMap;
import java.util.Map;

public class EuropeanaCursorPagination implements Pagination {

	public static final String FIRST_CURSOR = "*";

	public  static final String CURSOR_PARAM_NAME = "cursor";

	private String cursor;

	public EuropeanaCursorPagination() {
		this.cursor = FIRST_CURSOR;
	}

	public EuropeanaCursorPagination(String cursor) {
		this.cursor = cursor;
	}

	public String getCursor() {
		return cursor;
	}

	@Override
	public Map<String, String> getRequestParams() {
		Map<String, String> params = new HashMap<>();
		params.put(CURSOR_PARAM_NAME, this.cursor);
		return params;
	}

	public static String[] getRequestParamsNames() {
		return new String[]{CURSOR_PARAM_NAME};
	}
}
