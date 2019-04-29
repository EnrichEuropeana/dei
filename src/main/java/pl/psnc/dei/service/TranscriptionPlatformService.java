package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.model.DAO.DatasetsReposotory;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import reactor.core.publisher.Mono;

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

    private final WebClient webClient;
    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private DatasetsReposotory datasetsReposotory;
    private List<Project> availableProjects;
    private UrlBuilder urlBuilder;

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

    public void sendRecord(JsonObject record) {
        this.webClient.post()
                .uri(urlBuilder.urlForRecords())
                .body(BodyInserters.fromObject(record))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToMono(String.class)
                .block();
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
