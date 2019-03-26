package pl.psnc.dei.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.dei.model.Project;

public class UrlBuilderTest {

    @Test
    public void shouldReturnProperValues() {
        UrlBuilder urlBuilder = new UrlBuilder();
        ReflectionTestUtils.setField(urlBuilder, "transcriptionPlatformLocation", "sampleUrl");

        Assert.assertEquals("sampleUrl", urlBuilder.getBaseUrl());
        Assert.assertEquals("sampleUrl/Project/all", urlBuilder.urlForAllProjects());
        Project project = new Project();
        project.setProjectId("sampleProjectID");
        Assert.assertEquals("sampleUrl/Dataset/search?ProjectId=sampleProjectID", urlBuilder.urlForProjectDatasets(project));

    }
}