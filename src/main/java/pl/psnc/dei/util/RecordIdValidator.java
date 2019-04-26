package pl.psnc.dei.util;

public class RecordIdValidator {

	private static final String RECORD_ID_PATTERN = "^(?U)/\\p{Digit}+/[\\p{Graph}&&[^/]]+$";

	public static boolean validate(String recordId) {
		return recordId.matches(RECORD_ID_PATTERN);
	}
}
