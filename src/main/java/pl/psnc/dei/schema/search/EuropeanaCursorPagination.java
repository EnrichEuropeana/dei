package pl.psnc.dei.schema.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static pl.psnc.dei.util.EuropeanaConstants.*;

@AllArgsConstructor
public class EuropeanaCursorPagination implements Pagination {

	@Getter
	private String cursor;

	private String rows;

	public EuropeanaCursorPagination(String rows) {
		this.cursor = FIRST_CURSOR;
		this.rows = rows;
	}

	@Override
	public Map<String, String> getRequestParams() {
		Map<String, String> params = new HashMap<>();
		params.put(CURSOR_PARAM_NAME, this.cursor);
		params.put(ROWS_PARAM_NAME, this.rows);
		return params;
	}

	public static String[] getRequestParamsNames() {
		return new String[]{CURSOR_PARAM_NAME};
	}
}
