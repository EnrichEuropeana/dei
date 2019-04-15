package pl.psnc.dei.service;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.*;
import pl.psnc.dei.util.ImportNameCreatorUtil;

import java.util.List;

@Service
public class ImportPackageService {

    private ImportsRepository importsRepository;
    private RecordsRepository recordsRepository;
    private WebClient webClient;
    private UrlBuilder urlBuilder;

    @Autowired
    public ImportPackageService(ImportsRepository importsRepository, RecordsRepository recordsRepository,
                                WebClient webClient, UrlBuilder urlBuilder) {
        this.importsRepository = importsRepository;
        this.recordsRepository = recordsRepository;
        this.webClient = webClient;
        this.urlBuilder = urlBuilder;
    }

    /**
     * @param project project for searching candidates
     * @return list of records which are candidates
     */
    public List<Record> getCandidates(Project project, Dataset dataset) {
        if (dataset == null) {
            return recordsRepository.findAllByProjectAndDatasetNullAndAnImportNull(project);
        } else {
            return recordsRepository.findAllByProjectAndDatasetAndAnImportNull(project, dataset);
        }
    }

    /**
     * Create import for project from given records
     *
     * @param name    name of the import, if null then creating using project name and date
     * @param project project from which come records
     * @param records list of records to assign to the import
     */
    public Import createImport(String name, Project project, List<Record> records) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Import anImport = Import.from(ImportNameCreatorUtil.createDefaultImportName(name, project.getName()), records);
        anImport.setStatus(ImportStatus.CREATED);
        this.importsRepository.save(anImport);
        records.forEach(record -> {
            record.setAnImport(anImport);
            recordsRepository.save(record);
        });
        return anImport;
    }


    /**
     * Send import to TP
     *
     * @param inputImport input which should be send
     */
    public void sendExistingImport(Import inputImport) {
        Import anImport = importsRepository.getOne(inputImport.getId());
        anImport.setStatus(ImportStatus.IN_PROGRESS);
        importsRepository.save(anImport);
        HttpResponse response = webClient.post().uri(urlBuilder.urlForSendingImport()).body(BodyInserters.fromObject(anImport)).retrieve().bodyToMono(HttpResponse.class).block();
        if (response != null && response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
            anImport.setStatus(ImportStatus.SENT);
        } else {
            anImport.setStatus(ImportStatus.FAILED);
        }
        importsRepository.save(anImport);
        //TODO maybe change above code - the way we acquire http status
    }

    /**
     * @param inputImport object Import which content should be returned
     * @return import with records which belong to it
     */
    public Import getContentOfImport(Import inputImport) {
        Import anImport = importsRepository.getOne(inputImport.getId());
        anImport.setRecords(recordsRepository.findAllByAnImport(anImport));
        return anImport;
    }

    /**
     * @param inputImport object Import which status and failure should be returned
     * @return import status and import failure information
     */
    public ImportReport getStatusWithFailure(Import inputImport) {
        Import anImport = importsRepository.getOne(inputImport.getId());
        return ImportReport.from(anImport.getStatus(), anImport.getFailures());
    }

}
