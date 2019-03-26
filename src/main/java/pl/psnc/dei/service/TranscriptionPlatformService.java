package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;

import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for communication with Transcription Platform.
 *
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Service
public class TranscriptionPlatformService {

    private List<Project> availableProjects;
    private UrlBuilder urlBuilder;
    private final WebClient webClient;

    public TranscriptionPlatformService(UrlBuilder urlBuilder,
                                        WebClient.Builder webClientBuilder) {
        this.urlBuilder = urlBuilder;
        this.webClient = webClientBuilder.baseUrl(urlBuilder.getBaseUrl()).build();
    }

    public List<Project> getProjects() {
        if (!availableProjectInitialized()) {
            initAvailableProjects();
        }
        return availableProjects;
    }

    public void getDatasetsFor(Project project) {
        Dataset[] projectDatasets = this.webClient.get().uri(urlBuilder.urlForProjectDatasets(project)).retrieve().bodyToMono(Dataset[].class).block();

        for (Dataset projectDataset : projectDatasets) {
            projectDataset.setProject(project);
            project.getDatasets().add(projectDataset);
        }
    }

    public void getDatasetsFor(List<Project> projects) {
        for (Project project : projects) {
            getDatasetsFor(project);
        }
    }

    public void refreshAvailableProjects() {
        initAvailableProjects();
        getDatasetsFor(availableProjects);
    }

    private boolean availableProjectInitialized() {
        return availableProjects != null;
    }

    private void initAvailableProjects() {
        Project[] projects = this.webClient.get().uri(urlBuilder.urlForAllProjects()).retrieve().bodyToMono(Project[].class).block();
        availableProjects = Arrays.asList(projects);
    }

}
