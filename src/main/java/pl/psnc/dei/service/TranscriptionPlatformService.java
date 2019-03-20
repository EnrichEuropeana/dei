package pl.psnc.dei.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
        availableProjects = restTemplate.getForObject(urlBuilder.urlForProjectDatasets(project), List.class);
    }

    public void refreshAvailableProjects() {
        initAvailableProjects();
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
