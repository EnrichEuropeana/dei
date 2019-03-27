package pl.psnc.dei.model.DAO;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.UrlBuilder;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import({TranscriptionPlatformService.class, UrlBuilder.class})
public class RecordsRepositoryTest {

    @Autowired
    RecordsRepository recordsRepository;

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

        List<Record> records = recordsRepository.findAllByProject(project);

        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(record, records.get(0));
    }

    @Test
    public void findAllByDataset() {
        recordsRepository.save(record);

        List<Record> records = recordsRepository.findAllByDataset(dataset);

        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(record, records.get(0));
    }
}