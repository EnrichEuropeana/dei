package pl.psnc.dei.model.enrichments.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscribathonItemDTO {
    private String itemURL;

    private int pageNo;
}
