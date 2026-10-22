package pl.psnc.dei.util;

import lombok.experimental.UtilityClass;

import java.util.Date;

@UtilityClass
public class ImportNameCreatorUtil {

    private static final String TITLE_PATTERN = "IMPORT_%s_%tFT%tT";

    public static String generateImportName(String projectName) {
        return String.format(TITLE_PATTERN, projectName, new Date(), new Date());
    }

    public static boolean isMatchingImportTitlePattern(String text) {
        return text.matches("IMPORT_.+_[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}T[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");
    }
}
