package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.model.DAO.DatasetsReposotory;
import pl.psnc.dei.model.DAO.ProjectsRepository;
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

    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private DatasetsReposotory datasetsReposotory;

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
        saveAvailableProjects();
    }

    private boolean availableProjectInitialized() {
        return availableProjects != null;
    }

    private void initAvailableProjects() {
        Project[] projects = this.webClient.get().uri(urlBuilder.urlForAllProjects()).retrieve().bodyToMono(Project[].class).block();
        availableProjects = Arrays.asList(projects);
    }

    private void saveAvailableProjects() {
        for (Project project : availableProjects) {
            Project savedProject = projectsRepository.findByName(project.getName());
            if (savedProject == null) {
                savedProject = projectsRepository.save(project);
            }
            for (Dataset dataset : project.getDatasets()) {
                if (datasetsReposotory.findDatasetByDatasetId(dataset.getDatasetId()) == null) {
                    dataset.setProject(savedProject);
                    datasetsReposotory.save(dataset);
                }
            }
        }
    }

}
