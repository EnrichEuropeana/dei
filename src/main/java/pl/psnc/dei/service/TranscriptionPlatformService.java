package pl.psnc.dei.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;

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

    public TranscriptionPlatformService(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public List<Project> getProjects() {
        if (!availableProjectInitialized()) {
            initAvailableProjects();
        }
        return availableProjects;
    }

    public void getDatasetsFor(Project project) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Dataset>> rateResponse =
                restTemplate.exchange(urlBuilder.urlForProjectDatasets(project),
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Dataset>>() {
                        });
        List<Dataset> projectDatasets = rateResponse.getBody();
        for(Dataset projectDataset: projectDatasets){
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
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Project>> rateResponse =
                restTemplate.exchange(urlBuilder.urlForAllProjects(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Project>>() {
                        });
        availableProjects = rateResponse.getBody();
    }

}
