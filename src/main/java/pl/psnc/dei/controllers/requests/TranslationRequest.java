package pl.psnc.dei.controllers.requests;

import lombok.Data;

@Data
public class TranslationRequest {
    String xmlFolder;
    String translationsFolder;
    String fieldName;
    String mappingFile;
}
