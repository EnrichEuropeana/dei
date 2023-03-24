package pl.psnc.dei.controllers.responses;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CallToActionResponse {
    List<String> sent = new ArrayList<>();
    List<String> skipped = new ArrayList<>();
}
