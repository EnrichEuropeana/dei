package pl.psnc.dei.queue.task;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.*;
import pl.psnc.dei.model.DAO.DatasetsRepository;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.service.*;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.util.ArrayList;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
public class ConversionTaskTest {
    // TITLE: JOHN (NICKNAMED JACK) HENRY MALLETT'S TRAGIC DEATH
    // TP: https://europeana.transcribathon.eu/documents/story/item/?item=1180085
    private final String EUROPEANA_RECORD_IDENTIFIER = "/2020601/https___1914_1918_europeana_eu_contributions_17173";
    private Project project;
    private Dataset dataset;
    private Record record;

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
    private DDBFormatResolver ddbfr;

    @Autowired
    private TasksQueueService tqs;

    @Autowired
    private Converter converter;

    @Autowired
    private ImportProgressService ips;

    @Autowired
    private TasksFactory tasksFactory;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private DatasetsRepository datasetsRepository;

    @Autowired
    private ImportPackageService importPackageService;

    @Autowired
    private ImportProgressService importProgressService;

    @Autowired
    private ImportsRepository importsRepository;

    @Before
    public void initProject() {
        this.project = this.projectsRepository.findAll().get(0);
    }

    @Before
    public void initDataset() {
        this.dataset = this.datasetsRepository.findAll().get(0);
    }

    @Before
    public void initRecord() {
        this.record = new Record();
        this.record.setIdentifier(this.EUROPEANA_RECORD_IDENTIFIER);
        this.record.setAggregator(Aggregator.EUROPEANA);
        this.record.setProject(this.project);
        this.record.setDataset(this.dataset);
        this.record.setTranscriptions(new ArrayList<>());

        Import anImport = this.importPackageService.createImport("test", this.project.getProjectId(), Set.of(this.record));
        anImport.setProgress(
                this.importProgressService.initImportProgress(1)
        );
        this.importsRepository.save(anImport);

        this.record.setAnImport(anImport);

        this.qrs.saveRecord(this.record);
    }

    @SneakyThrows
    @Test
    public void runs() {
        // sadly conversion task has no own logic to test, everything is dispatched to other services
        ConversionTask conversionTask = new ConversionTask(this.record, this.qrs, this.tps, this.ess, this.eas, this.ddbfr, this.tqs, this.converter, this.ips, this.tasksFactory);
        conversionTask.process();
    }

}
