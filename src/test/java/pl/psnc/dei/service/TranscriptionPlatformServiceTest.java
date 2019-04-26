package pl.psnc.dei.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.model.DAO.DatasetsReposotory;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration
@TestPropertySource(properties = {
        "transcription.api.url=http://127.0.0.1:8181",
})
public class TranscriptionPlatformServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8181));

    @Autowired
    private TranscriptionPlatformService transcriptionPlatformService;

    @MockBean
    private ProjectsRepository projectsRepository;

    @MockBean
    private DatasetsReposotory datasetsReposotory;


    @TestConfiguration
    static class EmployeeServiceImplTestContextConfiguration {

        @Bean
        public UrlBuilder urlBuilder() {
            return new UrlBuilder();
        }

        @Bean
        public WebClient.Builder builder() {
            return WebClient.builder();
        }

        @Bean
        public TranscriptionPlatformService tpService(UrlBuilder urlBuilder, WebClient.Builder builder) {
            return new TranscriptionPlatformService(urlBuilder, builder);
        }
    }

    @Test
    public void shouldFetchListOfTranscriptions() throws TranscriptionPlatformException {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/records/123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"transcription\":\"test123\",\"target\":\"test321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"},{\"transcription\":\"test12345\",\"target\":\"test54321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"}]")));
        //
        Record testRecord = new Record();
        testRecord.setIdentifier("123");
        List<Transcription> transcriptions = transcriptionPlatformService.fetchTranscriptionsFor(testRecord);

        Assert.assertTrue(transcriptions.size() == 2);
        Assert.assertTrue(transcriptions.get(0).getTarget().equals("test321"));
        Assert.assertTrue(transcriptions.get(1).getTarget().equals("test54321"));

        Assert.assertTrue(transcriptions.get(0).getTranscription().equals("test123"));
        Assert.assertTrue(transcriptions.get(1).getTranscription().equals("test12345"));
    }


    @Test(expected = TranscriptionPlatformException.class)
    public void shouldFailOnTimeout() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/records/123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(6000)
                        .withBody("[{\"transcription\":\"test123\",\"target\":\"test321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"},{\"transcription\":\"test12345\",\"target\":\"test54321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"}]")));
        //
        Record testRecord = new Record();
        testRecord.setIdentifier("123");
        List<Transcription> transcriptions = transcriptionPlatformService.fetchTranscriptionsFor(testRecord);

        Assert.assertTrue(transcriptions.size() == 2);
        Assert.assertTrue(transcriptions.get(0).getTarget().equals("test321"));
        Assert.assertTrue(transcriptions.get(1).getTarget().equals("test54321"));

        Assert.assertTrue(transcriptions.get(0).getTranscription().equals("test123"));
        Assert.assertTrue(transcriptions.get(1).getTranscription().equals("test12345"));
    }

    @Test(expected = TranscriptionPlatformException.class)
    public void shouldFailOnServerError() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/records/123"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(2000)
                        .withBody("[{\"transcription\":\"test123\",\"target\":\"test321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"},{\"transcription\":\"test12345\",\"target\":\"test54321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"}]")));
        //
        Record testRecord = new Record();
        testRecord.setIdentifier("123");
        List<Transcription> transcriptions = transcriptionPlatformService.fetchTranscriptionsFor(testRecord);

        Assert.assertTrue(transcriptions.size() == 2);
        Assert.assertTrue(transcriptions.get(0).getTarget().equals("test321"));
        Assert.assertTrue(transcriptions.get(1).getTarget().equals("test54321"));

        Assert.assertTrue(transcriptions.get(0).getTranscription().equals("test123"));
        Assert.assertTrue(transcriptions.get(1).getTranscription().equals("test12345"));
    }

}