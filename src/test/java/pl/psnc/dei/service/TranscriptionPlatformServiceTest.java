package pl.psnc.dei.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.jena.atlas.json.JsonArray;
import org.junit.Assert;
import org.junit.Ignore;
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
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.queue.task.TasksFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration
@TestPropertySource(properties = {
        "transcription.api.url=http://127.0.0.1:8181",
})
public class TranscriptionPlatformServiceTest {

    @Test
    public void test() {

    }
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8181));

    @Autowired
    private TranscriptionPlatformService transcriptionPlatformService;

    @MockBean
    private ProjectsRepository projectsRepository;

    @MockBean
    private DatasetsReposotory datasetsReposotory;

    @MockBean
    private RecordsRepository recordsRepository;

    @MockBean
    private ImportsRepository importsRepository;

    @MockBean
    private TasksQueueService tasksQueueService;

    @MockBean
    private TasksFactory tasksFactory;

    @TestConfiguration
    static class TranscriptionPlatformServiceContextConfiguration {

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
        wireMockRule.stubFor(get(urlEqualTo("/enrichments?storyId=123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"transcription\":\"test123\",\"target\":\"test321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"},{\"transcription\":\"test12345\",\"target\":\"test54321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"}]")));
        //
        Record testRecord = new Record();
        testRecord.setIdentifier("123");
        JsonArray transcriptions = transcriptionPlatformService.fetchTranscriptionsFor(testRecord);

        Assert.assertEquals(2, transcriptions.size());
        Assert.assertEquals("test321", transcriptions.get(0).getAsObject().get("target").getAsString().value());
        Assert.assertEquals("test54321", transcriptions.get(1).getAsObject().get("target").getAsString().value());

        Assert.assertEquals("test123", transcriptions.get(0).getAsObject().get("transcription").getAsString().value());
        Assert.assertEquals("test12345", transcriptions.get(1).getAsObject().get("transcription").getAsString().value());
    }

    @Test(expected = TranscriptionPlatformException.class)
    public void shouldFailOnTimeoutWhileFetchingTranscriptions() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/enrichments?storyId=123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay((TranscriptionPlatformService.READ_TIMEOUT_IN_SECONDS + 1) * 1000)
                        .withBody("[{\"transcription\":\"test123\",\"target\":\"test321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"},{\"transcription\":\"test12345\",\"target\":\"test54321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"}]")));
        //
        Record testRecord = new Record();
        testRecord.setIdentifier("123");
        JsonArray transcriptions = transcriptionPlatformService.fetchTranscriptionsFor(testRecord);

        Assert.assertEquals(2, transcriptions.size());
        Assert.assertEquals("test321", transcriptions.get(0).getAsObject().get("target").getAsString().value());
        Assert.assertEquals("test54321", transcriptions.get(1).getAsObject().get("target").getAsString().value());

        Assert.assertEquals("test123", transcriptions.get(0).getAsObject().get("transcription").getAsString().value());
        Assert.assertEquals("test12345", transcriptions.get(1).getAsObject().get("transcription").getAsString().value());
    }

    @Test(expected = TranscriptionPlatformException.class)
    public void shouldFailOnServerErrorWhileFetchingTranscriptions() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/enrichments?storyId=123"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(2000)
                        .withBody("[{\"transcription\":\"test123\",\"target\":\"test321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"},{\"transcription\":\"test12345\",\"target\":\"test54321\",\"Timestamp\":\"Apr 22, 2019 12:50:57 PM\"}]")));
        //
        Record testRecord = new Record();
        testRecord.setIdentifier("123");
        transcriptionPlatformService.fetchTranscriptionsFor(testRecord);

        Assert.fail();
    }

    @Test(expected = TranscriptionPlatformException.class)
    public void shouldFailOnTimeoutWhileSendingAnnotationUrl() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlEqualTo("/enrichments/transcription/sampleIdentifierFromTP"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay((TranscriptionPlatformService.READ_TIMEOUT_IN_SECONDS + 1) * 1000)));

        //
        Transcription testTranscription = new Transcription();
        testTranscription.setTp_id("sampleIdentifierFromTP");
        testTranscription.setAnnotationId("sampleAnnotationId");
        transcriptionPlatformService.sendAnnotationUrl(testTranscription);

        Assert.fail();
    }

    @Test
    public void shouldSendAnnotationUrl() throws TranscriptionPlatformException {
        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlEqualTo("/enrichments/transcription/sampleIdentifierFromTP"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withFixedDelay(2000)));
        //
        Transcription testTranscription = new Transcription();
        testTranscription.setTp_id("sampleIdentifierFromTP");
        testTranscription.setAnnotationId("sampleAnnotationId");
        transcriptionPlatformService.sendAnnotationUrl(testTranscription);
    }

    @Test(expected = TranscriptionPlatformException.class)
    public void shouldFailOnServerErrorWhileWhileSendingAnnotationUrl() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(post(urlEqualTo("/enrichments/transcription/sampleIdentifierFromTP"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withFixedDelay(1000)));
        //
        Transcription testTranscription = new Transcription();
        testTranscription.setTp_id("sampleIdentifierFromTP");
        testTranscription.setAnnotationId("sampleAnnotationId");
        transcriptionPlatformService.sendAnnotationUrl(testTranscription);

        Assert.fail();
    }
}