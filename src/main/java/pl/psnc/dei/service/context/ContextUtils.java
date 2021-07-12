package pl.psnc.dei.service.context;

public class ContextUtils {
    public static void setIfPresent(Object toModify, Object value) {
        if (value != null) {
            toModify = value;
        }
    }

    public static void executeIfNotPresent(Object toCheck, Runnable function) {
        if (toCheck == null) {
            function.run();
        }
    }

    public static void executeIf(Boolean flag, Runnable function) {
        if (flag) {
            function.run();
        }
    }
}
