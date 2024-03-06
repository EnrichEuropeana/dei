package pl.psnc.dei.controllers.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CallToActionResponse {
    @Setter
    long executionTime;
    @Setter
    long updatedStories;
    List<String> sent = new ArrayList<>();
    List<String> skipped = new ArrayList<>();
}
