package pl.psnc.dei.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.dei.controllers.requests.CreateImportFromDatasetRequest;
import pl.psnc.dei.controllers.requests.UploadDatasetRequest;
import pl.psnc.dei.controllers.responses.CallToActionResponse;
import pl.psnc.dei.controllers.responses.ManifestRecreationResponse;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.IIIFManifestValidator;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "conversion.iiif.server.url=https://rhus-148.man.poznan.pl",
        "application.server.url=https://fresenia-dev.man.poznan.pl",
        "server.servlet.context-path=/dei-test",
})
public class BatchServiceTest {

    @Value("${conversion.iiif.server.url}")
    private String iiifServerUrl;

    @Value("${application.server.url}")
    private String serverUrl;

    @Value("${server.servlet.context-path}")
    private String serverPath;

    private static final String DATASET_ID = "202904";
    private static final String PROJECT_NAME = "EUROPEANA";
    private static final int LIMIT = 5;
    private static final Set<String> EXCLUDED = ImmutableSet.of("/123/abc", "/123/def", "/123/ghi");
    private static final int IMPORT_SIZE = 2;
    private static final Set<String> MOCKED_DATASET_RECORDS = Sets.newHashSet(
            "/123/abc", "/123/def", "/123/ghi", "/123/jkl", "/123/mno", "/123/pqr"
            , "/123/stu", "/123/vwx", "/123/yzz"
    );

    @Value("classpath:iiif/iiif_manifest.json")
    private Resource iiifManifest;

    @Value("classpath:iiif/iiif_good_manifest.json")
    private Resource iiifGoodManifest;

    @Value("classpath:tp/story_enrichments_response.json")
    private Resource storyEnrichments;

    private Record testRecord = new Record();
    private Record goodRecord = new Record();
    private Record actionRecord = new Record();

    @Before
    @SneakyThrows
    public void init() {
        testRecord.setIdentifier("/123/abc");
        Project project = new Project();
        project.setProjectId(PROJECT_NAME);
        project.setName(PROJECT_NAME);
        testRecord.setProject(project);
        testRecord.setAggregator(Aggregator.EUROPEANA);
        String manifest =
                String.join("",
                        IOUtils.readLines(
                                this.iiifManifest.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
        testRecord.setIiifManifest(manifest);

        goodRecord.setIdentifier("/137/_nnVV2z6");
        goodRecord.setProject(project);
        String goodManifest =
                String.join("",
                        IOUtils.readLines(
                                this.iiifGoodManifest.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
        goodRecord.setIiifManifest(goodManifest);
        goodRecord.setAggregator(Aggregator.EUROPEANA);

        actionRecord.setIdentifier("/111/abc");
        actionRecord.setProject(project);
        actionRecord.setAggregator(Aggregator.EUROPEANA);

        ReflectionTestUtils.setField(batchService, "iiifServerUrl", iiifServerUrl);
        ReflectionTestUtils.setField(batchService, "serverUrl", serverUrl);
        ReflectionTestUtils.setField(batchService, "serverPath", serverPath);
    }

    @InjectMocks @Spy private BatchService batchService;
    @Mock private EuropeanaSearchService europeanaSearchService;
    @Mock private ImportPackageService importPackageService;
    @Mock private RecordsRepository recordsRepository;
    @Mock private TranscriptionPlatformService transcriptionPlatformService;
    @Mock private IIIFManifestValidator iiifManifestValidator;
    @Mock private GeneralRestRequestService generalRestRequestService;
    @Mock private EuropeanaAnnotationsService europeanaAnnotationsService;

    @Rule public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldRejectMissingDatasetIdUploadDataset() throws NotFoundException {
        UploadDatasetRequest request = prepareUploadDatasetRequest();
        request.setEuropeanaDatasetId(null);
        exceptionRule.expect(IllegalArgumentException.class);
        batchService.uploadDataset(request);
    }

    @Test
    public void shouldRejectMissingProjectNameUploadDataset() throws NotFoundException {
        UploadDatasetRequest request = prepareUploadDatasetRequest();
        request.setProjectName(null);
        exceptionRule.expect(IllegalArgumentException.class);
        batchService.uploadDataset(request);
    }

    @Test
    public void shouldUploadRecordsFromDataset() throws NotFoundException {
        Mockito.doAnswer(BatchServiceTest::uploadRecordsAnswer)
                .when(batchService).uploadRecords(anyString(), any(), anySet());
        Mockito.when(europeanaSearchService.getAllDatasetRecords(anyString())).thenReturn(MOCKED_DATASET_RECORDS);
        UploadDatasetRequest request = prepareUploadDatasetRequest();
        Set<Record> records = batchService.uploadDataset(request);

        Set<String> recordsIds = records.stream()
                .map(Record::getIdentifier)
                .collect(Collectors.toSet());
        MOCKED_DATASET_RECORDS.forEach(id -> Assert.assertTrue(recordsIds.contains(id)));
    }

    @Test
    public void shouldExcludeRecordsFromUploadedDataset() throws NotFoundException {
        Mockito.doAnswer(BatchServiceTest::uploadRecordsAnswer)
                .when(batchService).uploadRecords(anyString(), any(), anySet());
        Mockito.when(europeanaSearchService.getAllDatasetRecords(anyString())).thenReturn(MOCKED_DATASET_RECORDS);
        UploadDatasetRequest request = prepareUploadDatasetRequest();
        request.setExcludedRecords(EXCLUDED);
        Set<Record> records = batchService.uploadDataset(request);

        Set<String> recordsIds = records.stream()
                .map(Record::getIdentifier)
                .collect(Collectors.toSet());

        Set<String> allRecordsMinusExcluded = new HashSet<>(MOCKED_DATASET_RECORDS);
        allRecordsMinusExcluded.removeAll(EXCLUDED);

        allRecordsMinusExcluded.forEach(id -> Assert.assertTrue(recordsIds.remove(id)));
        Assert.assertTrue(recordsIds.isEmpty());
    }

    @Test
    public void shouldLimitUploadedDataset() throws NotFoundException {
        Mockito.doAnswer(BatchServiceTest::uploadRecordsAnswer)
                .when(batchService).uploadRecords(anyString(), any(), anySet());
        Mockito.when(europeanaSearchService.getAllDatasetRecords(anyString())).thenReturn(MOCKED_DATASET_RECORDS);
        UploadDatasetRequest request = prepareUploadDatasetRequest();
        request.setLimit(LIMIT);
        Set<Record> records = batchService.uploadDataset(request);
        Assert.assertEquals(LIMIT, records.size());
    }

    @Test
    public void shouldSplitUploadedDatasetToSeveralImports() throws NotFoundException {
        Mockito.doAnswer(BatchServiceTest::uploadRecordsAnswer)
                .when(batchService).uploadRecords(anyString(), any(), anySet());
        Mockito.when(europeanaSearchService.getAllDatasetRecords(anyString())).thenReturn(MOCKED_DATASET_RECORDS);
        Mockito.when(importPackageService.createImport(anyString(), anyString(), anySet()))
                .thenAnswer(BatchServiceTest::createImportAnswer);
        CreateImportFromDatasetRequest request = prepareCreateImportFromDatasetRequest();
        request.setImportSize(2);
        List<Import> imports = batchService.createImportsFromDataset(request);

        imports.forEach(anImport -> Assert.assertTrue(anImport.getRecords().size() <= IMPORT_SIZE));
    }

    private CreateImportFromDatasetRequest prepareCreateImportFromDatasetRequest() {
        CreateImportFromDatasetRequest request = new CreateImportFromDatasetRequest();
        request.setProjectName(PROJECT_NAME);
        request.setEuropeanaDatasetId(DATASET_ID);
        return request;
    }

    private UploadDatasetRequest prepareUploadDatasetRequest() {
        return prepareCreateImportFromDatasetRequest();
    }

    private static Set<Record> uploadRecordsAnswer(InvocationOnMock invocationOnMock) {
        Set<String> recordIds = invocationOnMock.getArgument(2);
        return recordIds.stream()
                .map(id -> {
                    Record record = new Record();
                    record.setIdentifier(id);
                    Project project = new Project();
                    project.setProjectId(PROJECT_NAME);
                    project.setName(PROJECT_NAME);
                    record.setProject(project);
                    return record;
                }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Object createImportAnswer(InvocationOnMock invocationOnMock) {
        Import anImport = new Import();
        anImport.setName(invocationOnMock.getArgument(0));
        anImport.setRecords(invocationOnMock.getArgument(2));
        return anImport;
    }

    @Test
    public void shouldFixManifest() {
        Page<Record> page = new PageImpl<>(List.of(testRecord));
        Mockito.when(recordsRepository.findAllByIiifManifestNotNull(any())).thenReturn(page);
        Mockito.when(recordsRepository.count()).thenReturn(1L);

        ManifestRecreationResponse response = batchService.fixManifests();

        Assert.assertEquals(1L, response.getRecordsCount());
        Assert.assertEquals(1L, response.getRecordsWithManifest());
        Assert.assertEquals(1L, response.getRecreatedManifests());
    }

    @Test
    @SneakyThrows
    public void shouldNotFixManifest() {
        Mockito.when(recordsRepository.findByIdentifierAndIiifManifestNotNull(goodRecord.getIdentifier())).thenReturn(
                Optional.of(goodRecord));
        Mockito.when(recordsRepository.count()).thenReturn(1L);

        ManifestRecreationResponse response = batchService.fixManifest(goodRecord.getIdentifier());

        Assert.assertEquals(1L, response.getRecordsCount());
        Assert.assertEquals(1L, response.getRecordsWithManifest());
        Assert.assertEquals(0L, response.getRecreatedManifests());
    }

    @Test
    public void shouldSendCallToAction() {
        Page<Record> page = new PageImpl<>(List.of(actionRecord));
        Mockito.when(recordsRepository.findAllByStoryIdNull(any())).thenReturn(page);
        Mockito.when(recordsRepository.findAllByStoryIdNotNull(any())).thenReturn(page);
        Mockito.when(transcriptionPlatformService.retrieveStoryId(any())).thenCallRealMethod();
        Mockito.when(transcriptionPlatformService.fetchMetadataEnrichmentsFor(any())).thenReturn(readStoryEnrichmentsResponse());

        CallToActionResponse response = batchService.callToAction(false, true);

        Assert.assertEquals(1L, response.getSent().size());
        Assert.assertEquals(actionRecord.getIdentifier(), response.getSent().get(0));
    }

    @Test
    public void shouldNotSendCallToAction() {
        Page<Record> page = new PageImpl<>(List.of(actionRecord));
        Mockito.when(recordsRepository.findAllByStoryIdNull(any())).thenReturn(page);
        Mockito.when(recordsRepository.findAllByStoryIdNotNull(any())).thenReturn(page);
        Mockito.when(transcriptionPlatformService.retrieveStoryId(any())).thenCallRealMethod();
        Mockito.when(transcriptionPlatformService.fetchMetadataEnrichmentsFor(any())).thenReturn(readStoryEnrichmentsResponse());
        Mockito.doThrow(IllegalArgumentException.class).when(europeanaAnnotationsService).postCallToAction(any());

        CallToActionResponse response = batchService.callToAction(false, false);

        Assert.assertEquals(0L, response.getSent().size());
        Assert.assertEquals(1L, response.getSkipped().size());
        Assert.assertEquals(actionRecord.getIdentifier(), response.getSkipped().get(0));
    }

    @SneakyThrows
    private JsonValue readStoryEnrichmentsResponse() {
        return JSON.parseAny(String.join("",
                        IOUtils.readLines(
                                this.storyEnrichments.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                ));
    }
}
