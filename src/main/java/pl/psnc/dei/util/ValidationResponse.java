package pl.psnc.dei.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationResponse {
    @JsonProperty("okay")
    Integer okay;
    @JsonProperty("warnings")
    List<String> warnings;
    @JsonProperty("error")
    String error;
    @JsonProperty("url")
    String url;
}
