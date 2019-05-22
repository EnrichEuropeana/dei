package pl.psnc.dei.service;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class ImportPackageServiceTest {

    @InjectMocks
    ImportPackageService importPackageService = new ImportPackageService(WebClient.builder());
    @Mock
    private ImportsRepository importsRepository;
    @Mock
    private RecordsRepository recordsRepository;
    @Mock
    private ProjectsRepository projectsRepository;

    @Test
    public void shouldCreateImportWithGivenName() {
        //given
        Set<Record> records = new HashSet<>();
        records.add(new Record("id1"));
        records.add(new Record("id2"));
        Project project = new Project();
        project.setName("projectName");
        String projectId = "id";
        project.setProjectId(projectId);
        project.setRecords(records);
        String importName = "name";

        //when
        when(projectsRepository.findByProjectId(projectId)).thenReturn(project);
        Import impr = importPackageService.createImport(importName, projectId, records);

        //then
        Assert.assertEquals(impr.getName(), importName);
        verify(importsRepository, times(1)).save(argThat((Import anImport) -> anImport.getName().equals(importName)));
    }

    @Test
    public void shouldCreateImportWithDefaultName() {
        //given
        String regexDefaultProjectName = "IMPORT_projectName_[0-9]{4}-[0-9]{2}-[0-9]{2}.*";
        Set<Record> records = new HashSet<>();
        records.add(new Record("id1"));
        records.add(new Record("id2"));
        Project project = new Project();
        project.setName("projectName");
        String projectId = "id";
        project.setRecords(records);

        //when
        when(projectsRepository.findByProjectId(projectId)).thenReturn(project);
        Import impr = importPackageService.createImport("", projectId, records);

        //then
        Assert.assertTrue(impr.getName().matches(regexDefaultProjectName));
        verify(importsRepository, times(1)).save(argThat((Import anImport) -> {
            return anImport.getName().matches(regexDefaultProjectName);
        }));
    }
}