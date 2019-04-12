package pl.psnc.dei.service;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class ImportPackageServiceTest {

    @InjectMocks
    ImportPackageService importPackageService;
    @Mock
    private ImportsRepository importsRepository;
    @Mock
    private RecordsRepository recordsRepository;

    @Test
    public void shouldCreateImportWithGivenName() {
        //given
        List<Record> records = Lists.list(new Record("id1"), new Record("id2"));
        Project project = new Project();
        project.setName("projectName");
        project.setRecords(records);
        String importName = "name";

        //when
        Import impr = importPackageService.createImport(importName, project, records);

        //then
        Assert.assertEquals(impr.getName(), importName);
        verify(importsRepository, times(1)).save(argThat((Import anImport) -> anImport.getName().equals(importName)));
        verify(recordsRepository, times(2)).save(Mockito.any(Record.class));
    }

    @Test
    public void shouldCreateImportWithDefaultName() {
        //given
        String regexDefaultProjectName = "projectName_[0-9]{4}-[0-9]{2}-[0-9]{2}.*";
        List<Record> records = Lists.list(new Record("id1"), new Record("id2"));
        Project project = new Project();
        project.setName("projectName");
        project.setRecords(records);

        //when
        Import impr = importPackageService.createImport("", project, records);

        //then
        Assert.assertTrue(impr.getName().matches(regexDefaultProjectName));
        verify(importsRepository, times(1)).save(argThat((Import anImport) -> {
            return anImport.getName().matches(regexDefaultProjectName);
        }));
        verify(recordsRepository, times(2)).save(Mockito.any(Record.class));
    }
}