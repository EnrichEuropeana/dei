package pl.psnc.dei.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RecordsProjectsAssignmentServiceTest {

    @InjectMocks
    private RecordsProjectsAssignmentService recordsProjectsAssignmentService;

    @Mock
    private RecordsRepository recordsRepository;

    private Project project;

    private Dataset dataset;

    private Record record;

    @Before
    public void setUp() {
        project = new Project();
        project.setProjectId("Project1");

        dataset = new Dataset();
        dataset.setProject(project);
        dataset.setDatasetId("Dataset1");

        record = new Record("id1");
        when(recordsRepository.save(record)).thenReturn(record);

        List<Record> records = new ArrayList<>();
        records.add(record);
        when(recordsRepository.findAllByProject(any(Project.class))).thenReturn(records);
        when(recordsRepository.findAllByDataset(any(Dataset.class))).thenReturn(records);
    }

    @Test
    public void assignRecordsToProject() {
        List<Record> records = new ArrayList<>();
        records.add(record);

        recordsProjectsAssignmentService.assignRecords(records, project, null);

        assertEquals("id1", record.getIdentifier());
        assertNotNull(record.getProject());
        assertEquals(project, record.getProject());
        assertNull(record.getDataset());
    }

    @Test
    public void assignRecordsToProjectAndDataset() {
        List<Record> records = new ArrayList<>();
        records.add(record);

        recordsProjectsAssignmentService.assignRecords(records, project, dataset);

        assertEquals("id1", record.getIdentifier());
        assertNotNull(record.getProject());
        assertEquals(project, record.getProject());
        assertEquals(dataset, record.getDataset());
    }

    @Test(expected = NullPointerException.class)
    public void assignRecordsToNullProject() {
        List<Record> records = new ArrayList<>();
        records.add(record);

        recordsProjectsAssignmentService.assignRecords(records, null, dataset);
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void assignRecordsToProjectWhenRecordsNull() {
        recordsProjectsAssignmentService.assignRecords(null, project, dataset);
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assignRecordsToDatasetNotRelatedToProject() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        dataset.setProject(new Project());

        recordsProjectsAssignmentService.assignRecords(records, project, dataset);
        fail();
    }

    @Test
    public void unassignRecordsFromProject() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        record.setDataset(dataset);
        record.setProject(project);

        recordsProjectsAssignmentService.unassignRecords(records, project);

        assertNull(record.getProject());
        assertNull(record.getDataset());
    }

    @Test
    public void unassignRecordsFromProjectWhenDifferentProject() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        record.setDataset(dataset);
        record.setProject(new Project());

        recordsProjectsAssignmentService.unassignRecords(records, project);

        assertNotNull(record.getProject());
        assertNotNull(record.getDataset());
        assertNotEquals(project, record.getProject());
    }

    @Test(expected = NullPointerException.class)
    public void unassignRecordsWhenRecordsNull() {
        recordsProjectsAssignmentService.unassignRecords(null, project);
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void unassignRecordsFromNullProject() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        record.setProject(project);

        recordsProjectsAssignmentService.unassignRecords(records, (Project) null);
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void unassignRecordsFromNullDataset() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        record.setProject(project);
        record.setDataset(dataset);

        recordsProjectsAssignmentService.unassignRecords(records, (Dataset) null);
        fail();
    }

    @Test
    public void unassignRecordsFromDataset() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        record.setProject(project);
        record.setDataset(dataset);

        recordsProjectsAssignmentService.unassignRecords(records, dataset);

        assertNull(record.getDataset());
        assertNotNull(record.getProject());
    }

    @Test
    public void unassignRecordsFromDatasetWhenProjectNotMatch() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        record.setProject(new Project());
        record.setDataset(dataset);

        recordsProjectsAssignmentService.unassignRecords(records, dataset);

        assertNotNull(record.getDataset());
        assertEquals(dataset, record.getDataset());
        assertNotNull(record.getProject());
    }

    @Test
    public void unassignRecordsFromDatasetWhenDatasetNotMatch() {
        List<Record> records = new ArrayList<>();
        records.add(record);
        record.setProject(project);
        record.setDataset(new Dataset());

        recordsProjectsAssignmentService.unassignRecords(records, dataset);

        assertNotNull(record.getDataset());
        assertNotEquals(dataset, record.getDataset());
    }

    @Test
    public void getAssignedRecordsForProject() {
        record.setProject(project);

        List<Record> records = recordsProjectsAssignmentService.getAssignedRecords(project);

        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(record, records.get(0));
    }

    @Test
    public void getAssignedRecordsForDataset() {
        record.setProject(project);
        record.setDataset(dataset);

        List<Record> records = recordsProjectsAssignmentService.getAssignedRecords(dataset);

        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(record, records.get(0));
    }
}