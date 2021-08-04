package pl.psnc.dei.queue.task;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.util.Set;

import static org.junit.Assert.assertEquals;

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

    @Qualifier("transcriptionPlatformService")
    @Autowired
    private TranscriptionPlatformService tps;

    @Autowired
    private EuropeanaSearchService ess;

    @Autowired
    private EuropeanaAnnotationsService eas;

    @Autowired
    private TasksQueueService tqs;

    @Autowired
    private TasksFactory tasksFactory;

    @Autowired
    private Converter converter;

    @Autowired
    private DDBFormatResolver ddbfr;

    @Before
    public void prepareDatasets() {
        this.nonIiifDataset = this.datasetsRepository.findAll().get(0);
        this.iiifDataset = this.datasetsRepository.findAll().get(0);
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
        TranscribeTask transcribeTask = new TranscribeTask(this.recordWithoutIIIF, this.qrs, this.tps, this.ess, this.eas, this.tqs, this.serverUrl, this.serverPath, this.importProgressService, this.tasksFactory);
        transcribeTask.process();
        assertEquals(
                Record.RecordState.C_PENDING,
                this.recordsRepository.findByIdentifier(this.NON_IIIF_RECORD_IDENTIFIER).get().getState()
        );

        ConversionTask conversionTask = new ConversionTask(this.recordWithoutIIIF, this.qrs, this.tps, this.ess, this.eas, this.ddbfr, this.tqs, this.converter, this.importProgressService, this.tasksFactory);
        conversionTask.process();
        assertEquals(
                Record.RecordState.T_PENDING,
                this.recordsRepository.findByIdentifier(this.NON_IIIF_RECORD_IDENTIFIER).get().getState()
        );

        transcribeTask = new TranscribeTask(this.recordWithoutIIIF, this.qrs, this.tps, this.ess, this.eas, this.tqs, this.serverUrl, this.serverPath, this.importProgressService, this.tasksFactory);
        transcribeTask.process();
        assertEquals(
                Record.RecordState.T_SENT,
                this.recordsRepository.findByIdentifier(this.NON_IIIF_RECORD_IDENTIFIER).get().getState()
        );
    }

    @Test
    @Rollback
    @Transactional
    public void transcribeRecordWithIIIFPresent() {
        TranscribeTask transcribeTask = new TranscribeTask(this.recordWithIIIF, this.qrs, this.tps, this.ess, this.eas, this.tqs, this.serverUrl, this.serverPath, this.importProgressService, this.tasksFactory);
        transcribeTask.process();
        assertEquals(
                Record.RecordState.T_SENT,
                this.recordsRepository.findByIdentifier(this.IIIF_RECORD_IDENTIFIER).get().getState()
        );
    }
}
