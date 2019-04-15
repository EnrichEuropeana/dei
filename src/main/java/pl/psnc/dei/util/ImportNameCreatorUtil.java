package pl.psnc.dei.util;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class ImportNameCreatorUtil {

    private static final String SPACE_SEPARATOR = " ";
    private static final String UNDERSCORE_SEPARATOR = "_";
    private static final String IMPORT = "IMPORT_";

    private ImportNameCreatorUtil() {

    }

    public static String createDefaultImportName(String name, String projectName) {
        return name.isEmpty() ? IMPORT + StringUtils.replace(projectName, SPACE_SEPARATOR, UNDERSCORE_SEPARATOR) + UNDERSCORE_SEPARATOR + getCurrentDate() : name;
    }

    private static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-ddKK:mm:ssZ").format(Date.from(Instant.now()));
    }

}
