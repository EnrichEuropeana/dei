package pl.psnc.dei.service.translation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class TranslationsDTO {

    @JsonProperty("description")
    private List<String> originalValues = new ArrayList<>();
    @JsonProperty("language")
    private List<String> detectedLanguages = new ArrayList<>();
    @JsonProperty("translation")
    private List<String> translations = new ArrayList<>();

    @JsonProperty
    private String tool;

    @JsonProperty
    private String recordId;

    @JsonProperty
    private String identifier;

    @JsonProperty
    private String translationStatus;

    @JsonProperty
    private long modified;
}