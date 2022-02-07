package pl.psnc.dei.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.controllers.requests.CreateImportFromDatasetRequest;
import pl.psnc.dei.controllers.requests.UploadDatasetRequest;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;

@RunWith(SpringRunner.class)
public class BatchServiceTest {

    private static final String DATASET_ID = "202904";
    private static final String PROJECT_NAME = "EUROPEANA";
    private static final int LIMIT = 5;
    private static final Set<String> EXCLUDED = ImmutableSet.of("/123/abc", "/123/def", "/123/ghi");
    private static final int IMPORT_SIZE = 2;
    private static final Set<String> MOCKED_DATASET_RECORDS = Sets.newHashSet(
            "/123/abc", "/123/def", "/123/ghi", "/123/jkl", "/123/mno", "/123/pqr"
            , "/123/stu", "/123/vwx", "/123/yzz"
    );

    @InjectMocks @Spy private BatchService batchService;
    @Mock private EuropeanaSearchService europeanaSearchService;
    @Mock private ImportPackageService importPackageService;

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
}
