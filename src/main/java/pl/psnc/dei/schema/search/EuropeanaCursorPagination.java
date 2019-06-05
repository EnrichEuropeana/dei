package pl.psnc.dei.schema.search;

import java.util.HashMap;
import java.util.Map;

import static pl.psnc.dei.util.EuropeanaConstants.CURSOR_PARAM_NAME;
import static pl.psnc.dei.util.EuropeanaConstants.FIRST_CURSOR;

public class EuropeanaCursorPagination implements Pagination {

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
