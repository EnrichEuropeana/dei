package pl.psnc.dei.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class TranscriptionTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Transcription transcription;

    @Before
    public void initTranscription() {
        this.transcription = new Transcription();
        this.transcription.setTp_id("32423ewrf2wef");
        this.transcription.setAnnotationId("dg32ewrfwe3rf");
    }

    @Test
    public void appliesChangedProperty() throws JsonProcessingException {
        String asJson = this.objectMapper.writeValueAsString(this.transcription);
        assertTrue(asJson.contains("EuropeanaAnnotationId"));
    }
}
