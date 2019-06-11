package pl.psnc.dei.schema.search;

import java.util.HashMap;
import java.util.Map;

import static pl.psnc.dei.ui.components.SearchResultsComponent.DEFAULT_PAGE_SIZE;

public class DDBOffsetPagination implements Pagination {

	private static final String ROWS_PARAM_NAME = "rows";
	private static final String OFFSET_PARAM_NAME = "offset";

	private int rows;
	private int offset;

	public DDBOffsetPagination() {
		this.rows = DEFAULT_PAGE_SIZE;
		this.offset = 0;
	}

	public DDBOffsetPagination(int rows, int offset) {
		this.rows = rows;
		this.offset = offset;
	}

	@Override
	public Map<String, String> getRequestParams() {
		Map<String, String> params = new HashMap<>();
		params.put(ROWS_PARAM_NAME, String.valueOf(this.rows));
		params.put(OFFSET_PARAM_NAME, String.valueOf(this.offset));
		return params;
	}

	public static String[] getRequestParamsNames() {
		return new String[]{ROWS_PARAM_NAME, OFFSET_PARAM_NAME};
	}
}
