package pl.psnc.dei.model;

import java.util.Arrays;
import java.util.Objects;

public enum TranscriptionType {
    MANUAL,
    HTR;

    public static TranscriptionType from(String type) {
        Objects.requireNonNull(type);
        return Arrays.stream(values()).filter(value -> value.toString().equalsIgnoreCase(type)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown transcription type %s", type)));
    }
}
