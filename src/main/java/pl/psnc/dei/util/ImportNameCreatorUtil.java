package pl.psnc.dei.util;

import java.util.Date;

public class ImportNameCreatorUtil {

    private static final String TITLE_PATTERN = "IMPORT_%s_%tFT%tT";


    private ImportNameCreatorUtil() {

    }

    public static String generateImportName(String value) {
        return String.format(TITLE_PATTERN, value, new Date(), new Date());
    }

    public static boolean isMatchingImportTitlePattern(String text) {
        return text.matches("IMPORT_[A-Za-z]+_[0-9]+-[0-9]+-[0-9]+T[0-9]+:[0-9]+:[0-9]+");
    }
}
