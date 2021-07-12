package pl.psnc.dei.service.context;

import java.util.Collection;

public class ContextUtils {
    public static <T> void setIfPresent(T toModify, T value) {
        // actually java is not working like this
        if (value != null) {
            toModify = value;
        }
    }

    public static void executeIfPresent(Object toCheck, Runnable function) {
        if (toCheck != null) {
            function.run();
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

    public static void executeIfNotEmpty(Collection<?> collection, Runnable function) {
        if (collection.size() > 0) {
            function.run();
        }
    }

    public static void executeIfEmpty(Collection<?> collection, Runnable function) {
        if (collection.isEmpty()) {
            function.run();
        }
    }
}
