package pl.psnc.dei.queue.task;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.*;
import pl.psnc.dei.model.DAO.DatasetsRepository;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.context.ContextMediator;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.IIIFManifestValidator;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
public class TranscribeTaskTest {
    // https://www.europeana.eu/en/item/9200520/12148_bpt6k937789t?utm_source=api&utm_medium=api&utm_campaign=api2demo
    // Próbki gwary Mazowieckiej z konca XVII i poczatku XVIII wieku / podal Dr. Boleslaw Erzepki
    private final String IIIF_RECORD_IDENTIFIER = "/9200520/12148_bpt6k937789t";
    private final String IIIF_IMPORT_NAME = "IIIFtestName";
    // https://europeana.transcribathon.eu/documents/story/item/?item=1180085
    // JOHN (NICKNAMED JACK) HENRY MALLETT'S TRAGIC DEATH
    private final String NON_IIIF_RECORD_IDENTIFIER = "/2020601/https___1914_1918_europeana_eu_contributions_17173";
    private final String NON_IIIF_IMPORT_NAME = "testName";
    @Value("${application.server.url}")
    String serverUrl;
    private Record recordWithIIIF;
    private Import iiifImport;
    private Dataset iiifDataset;
    private Record recordWithoutIIIF;
    private Import nonIiifImport;
    private Dataset nonIiifDataset;
    private Project project;
    @Value("${server.servlet.context-path}")
    private String serverPath;

    @Autowired
    private RecordsRepository recordsRepository;

    @Autowired
    private ImportsRepository importsRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private ImportProgressService importProgressService;

    @Autowired
    private ImportPackageService importPackageService;

    @Autowired
    private DatasetsRepository datasetsRepository;

    @Autowired
    private QueueRecordService qrs;

    @MockBean
    private TranscriptionPlatformService tps;

    @MockBean
    private EuropeanaSearchService ess;

    @MockBean
    private EuropeanaAnnotationsService eas;

    @Autowired
    private TasksQueueService tqs;

    @Autowired
    private TasksFactory tasksFactory;

    @MockBean
    private Converter converter;

    @Autowired
    private DDBFormatResolver ddbfr;

    @Autowired
    private ContextMediator contextMediator;

    @Autowired
    private PersistableExceptionService persistableExceptionService;

    @Autowired
    IIIFManifestValidator imv;

    @Autowired
    GeneralRestRequestService grrs;

    @Before
    public void prepareMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.tps).sendRecord(any(), any());
        doNothing().when(this.tps).updateImportState(any());
        doNothing().when(this.eas).postCallToAction(any());

        org.apache.jena.atlas.json.JsonObject nonIiifJson = org.apache.jena.atlas.json.JSON.parse("{\"@graph\":[]}");
        org.apache.jena.atlas.json.JsonObject iiifJson = org.apache.jena.atlas.json.JSON.parse("{\"@graph\":[{\"@type\":\"svcs:Service\",\"conformsTo\":\"http://iiif.io/api/image\",\"@id\":\"http://iiif.service/1\"},{\"@type\":\"edm:WebResource\",\"svcs:has_service\":{\"@id\":\"http://iiif.service/1\"},\"dcterms:isReferencedBy\":{\"@id\":\"https://iiif.test/manifest\"}}]}");
        org.apache.jena.atlas.json.JsonObject rawJson = org.apache.jena.atlas.json.JSON.parse("{}");

        org.mockito.Mockito.when(this.ess.retrieveRecordAndConvertToJsonLd(org.mockito.ArgumentMatchers.eq(NON_IIIF_RECORD_IDENTIFIER)))
                .thenReturn(nonIiifJson);
        org.mockito.Mockito.when(this.ess.retrieveRecordAndConvertToJsonLd(org.mockito.ArgumentMatchers.eq(IIIF_RECORD_IDENTIFIER)))
                .thenReturn(iiifJson);
        org.mockito.Mockito.when(this.ess.retrieveRecordInJson(any()))
                .thenReturn(rawJson);

        org.mockito.Mockito.doAnswer(inv -> {
            Record r = inv.getArgument(0);
            r.setIiifManifest("https://example.com/manifest");
            return null;
        }).when(this.converter).convertAndGenerateManifest(any(), any(), any());
    }

    @Before
    public void prepareDatasets() {
        if (this.projectsRepository.count() == 0) {
            Project p = new Project();
            p.setName("test-project");
            p.setProjectId("test-project-id");
            p.setDatasets(new java.util.ArrayList<>());
            this.project = this.projectsRepository.save(p);
        } else {
            this.project = this.projectsRepository.findAll().get(0);
        }

        if (this.datasetsRepository.count() == 0) {
            Dataset d = new Dataset();
            d.setDatasetId("test-dataset-id");
            d.setName("test-dataset");
            d.setProject(this.project);
            this.nonIiifDataset = this.datasetsRepository.save(d);
            this.iiifDataset = this.nonIiifDataset;
            this.project.getDatasets().add(this.nonIiifDataset);
            this.projectsRepository.save(this.project);
        } else {
            this.nonIiifDataset = this.datasetsRepository.findAll().get(0);
            this.iiifDataset = this.datasetsRepository.findAll().get(0);
        }
    }

    @Before
    public void prepareRecordWithoutIIIF() {
        // without IIIF
        this.project = this.projectsRepository.findAll().get(0);
        this.recordWithoutIIIF = new Record();
        this.recordWithoutIIIF.setIdentifier(this.NON_IIIF_RECORD_IDENTIFIER);
        this.recordWithoutIIIF.setAggregator(Aggregator.EUROPEANA);
        this.recordWithoutIIIF.setProject(this.project);
        this.recordWithoutIIIF.setDataset(this.nonIiifDataset);
        this.recordWithoutIIIF.setState(Record.RecordState.T_PENDING);
        this.recordWithoutIIIF.setValidated(true);
        this.recordWithoutIIIF = this.recordsRepository.save(this.recordWithoutIIIF);
        this.nonIiifImport = this.importPackageService.createImport(this.NON_IIIF_IMPORT_NAME, this.nonIiifDataset.getProject().getProjectId(), Set.of(this.recordWithoutIIIF));
        this.nonIiifImport.setProgress(this.importProgressService.initImportProgress(1));
        this.nonIiifImport = this.importsRepository.save(this.nonIiifImport);
        this.recordWithoutIIIF.setAnImport(this.nonIiifImport);
        this.recordsRepository.save(this.recordWithoutIIIF);
    }

    @Before
    public void prepareRecordWithIIIF() {
        this.recordWithIIIF = new Record();
        this.recordWithIIIF.setIdentifier(this.IIIF_RECORD_IDENTIFIER);
        this.recordWithIIIF.setAggregator(Aggregator.EUROPEANA);
        this.recordWithIIIF.setProject(this.project);
        this.recordWithIIIF.setDataset(this.iiifDataset);
        this.recordWithIIIF.setState(Record.RecordState.T_PENDING);
        this.recordWithIIIF.setValidated(true);
        this.recordWithIIIF = this.recordsRepository.save(this.recordWithIIIF);
        this.iiifImport = this.importPackageService.createImport(this.IIIF_IMPORT_NAME, this.iiifDataset.getProject().getProjectId(), Set.of(this.recordWithIIIF));
        this.iiifImport.setProgress(this.importProgressService.initImportProgress(1));
        this.iiifImport = this.importsRepository.save(this.iiifImport);
        this.recordWithIIIF.setAnImport(this.iiifImport);
        this.recordsRepository.save(this.recordWithIIIF);
    }

    @SneakyThrows
    @Test
    @Rollback
    @Transactional
    public void transcribeRecordWithoutIIIFPresent() {
        TranscribeTask transcribeTask = new TranscribeTask(this.recordWithoutIIIF, this.qrs, this.tps, this.ess, this.eas, this.tqs, this.serverUrl, this.serverPath, this.tasksFactory, this.contextMediator, this.persistableExceptionService, this.importProgressService);
        transcribeTask.process();
        assertEquals(
                Record.RecordState.C_PENDING,
                this.recordsRepository.findByIdentifier(this.NON_IIIF_RECORD_IDENTIFIER).get().getState()
        );

        ConversionTask conversionTask = new ConversionTask(this.recordWithoutIIIF, this.qrs, this.tps, this.ess, this.eas, this.ddbfr, this.tqs, this.converter, this.importProgressService, this.tasksFactory, this.persistableExceptionService, this.recordsRepository, this.contextMediator);
        conversionTask.process();
        assertEquals(
                Record.RecordState.T_PENDING,
                this.recordsRepository.findByIdentifier(this.NON_IIIF_RECORD_IDENTIFIER).get().getState()
        );

        transcribeTask = new TranscribeTask(this.recordWithoutIIIF, this.qrs, this.tps, this.ess, this.eas, this.tqs, this.serverUrl, this.serverPath, this.tasksFactory, this.contextMediator, this.persistableExceptionService, this.importProgressService);
        transcribeTask.process();
        assertEquals(
                Record.RecordState.T_SENT,
                this.recordsRepository.findByIdentifier(this.NON_IIIF_RECORD_IDENTIFIER).get().getState()
        );
    }

    @Test
    @Rollback
    @Transactional
    public void transcribeRecordWithIIIFPresent() throws Exception {
        TranscribeTask transcribeTask = new TranscribeTask(this.recordWithIIIF, this.qrs, this.tps, this.ess, this.eas, this.tqs, this.serverUrl, this.serverPath, this.tasksFactory, this.contextMediator, this.persistableExceptionService, this.importProgressService);
        transcribeTask.process();
        assertEquals(
                Record.RecordState.T_SENT,
                this.recordsRepository.findByIdentifier(this.IIIF_RECORD_IDENTIFIER).get().getState()
        );
    }
}
