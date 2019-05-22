package pl.psnc.dei.model.DAO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.TranscriptionPlatformService;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RecordsRepositoryTest {

    /** This is just to ensure that the test is running successfully. Bean of this class is needed. */
    @MockBean
    TranscriptionPlatformService transcriptionPlatformService;

    @Autowired
    RecordsRepository recordsRepository;

    @Autowired
    DatasetsReposotory datasetsReposotory;

    private Record record;

    private Project project;

    private Dataset dataset;

    @Before
    public void setUp() {
        record = new Record("id1");
        project = new Project();
        project.setProjectId("Project1");
        record.setProject(project);

        dataset = new Dataset();
        dataset.setProject(project);
        dataset.setDatasetId("Dataset1");
        record.setDataset(dataset);
    }

    @Test
    public void findAllByProject() {
        recordsRepository.save(record);
        datasetsReposotory.save(dataset);

        List<Record> records = recordsRepository.findAllByProject(project);

        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(record, records.get(0));
    }

    @Test
    public void findAllByDataset() {
        recordsRepository.save(record);
        datasetsReposotory.save(dataset);

        List<Record> records = recordsRepository.findAllByDataset(dataset);

        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(record, records.get(0));
    }
}