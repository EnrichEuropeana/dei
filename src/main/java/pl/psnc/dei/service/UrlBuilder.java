package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Project;

/**
 * Class responsible for building urls to different endpoints;
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Service
public class UrlBuilder {

    private static final String ALL_PROJECTS_SUFFIX = "/Project/all";
    private static final String DATASETS_SEARCH_SUFFIX = "/Dataset/search";
    private static final String IMPORTS_ADD_SUFFIX = "/";

    @Value("${transcription.api.url}")
    private String transcriptionPlatformLocation;

    public String getBaseUrl() {
        return transcriptionPlatformLocation;
    }

    public String urlForAllProjects() {
        return transcriptionPlatformLocation + ALL_PROJECTS_SUFFIX;
    }

    public String urlForProjectDatasets(Project project) {
        return transcriptionPlatformLocation + DATASETS_SEARCH_SUFFIX + "?ProjectId=" + project.getProjectId();
    }

    public String urlForSendingImport(){
        return transcriptionPlatformLocation + IMPORTS_ADD_SUFFIX;
        //todo change to real url
    }

}
