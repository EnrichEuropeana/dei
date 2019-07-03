package pl.psnc.dei.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

public class UrlBuilderTest {

    @Test
    public void shouldReturnProperValues() {
        UrlBuilder urlBuilder = new UrlBuilder();
        ReflectionTestUtils.setField(urlBuilder, "transcriptionPlatformLocation", "sampleUrl");

        Assert.assertEquals("sampleUrl", urlBuilder.getBaseUrl());
        Assert.assertEquals("sampleUrl/projects", urlBuilder.urlForAllProjects());
        Project project = new Project();
        project.setProjectId("sampleProjectID");
        Assert.assertEquals("sampleUrl/projects/sampleProjectID/datasets", urlBuilder.urlForProjectDatasets(project));
        Record record = new Record();
        record.setProject(project);
        Import anImport = new Import();
        anImport.setName("sampleImport");
        record.setAnImport(anImport);
        Dataset dataset = new Dataset();
        dataset.setDatasetId("sampleDatasetId");
        dataset.setProject(project);
        Assert.assertEquals("sampleUrl/projects/sampleProjectID/stories?importName=sampleImport", urlBuilder.urlForSendingRecord(record));
        record.setDataset(dataset);
        Assert.assertEquals("sampleUrl/projects/sampleProjectID/stories?importName=sampleImport&datasetId=sampleDatasetId", urlBuilder.urlForSendingRecord(record));
    }
}