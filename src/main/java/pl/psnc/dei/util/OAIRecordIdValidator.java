package pl.psnc.dei.util;

public class OAIRecordIdValidator {

	private OAIRecordIdValidator() {}

	private static final String RECORD_ID_PATTERN = "^oai:[a-zA-Z][a-zA-Z0-9\\-]*(\\.[a-zA-Z][a-zA-Z0-9\\-]*)+:.+$";

	public static boolean validate(String recordId) {
		return recordId.matches(RECORD_ID_PATTERN);
	}
}
