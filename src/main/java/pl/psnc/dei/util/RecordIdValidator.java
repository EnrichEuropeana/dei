package pl.psnc.dei.util;

public class RecordIdValidator {

	private static final String RECORD_ID_PATTERN = "^/[0-9]+/[0-9a-zA-Z_]+$";

	public static boolean validate(String recordId) {
		return recordId.matches(RECORD_ID_PATTERN);
	}
}
