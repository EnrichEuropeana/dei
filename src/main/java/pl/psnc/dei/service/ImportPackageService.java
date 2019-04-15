package pl.psnc.dei.service;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.DatasetsReposotory;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.*;
import pl.psnc.dei.request.RestRequestExecutor;

import java.util.List;

import static pl.psnc.dei.util.ImportNameCreatorUtil.generateImportName;


@Service
public class ImportPackageService extends RestRequestExecutor {

    private final static Logger log = LoggerFactory.getLogger(ImportPackageService.class);

    @Autowired
    private ImportsRepository importsRepository;
    @Autowired
    private RecordsRepository recordsRepository;
    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private DatasetsReposotory datasetsReposotory;
    @Autowired
    private UrlBuilder urlBuilder;

    public static String createImportName(String name, String projectName) {
        return name.isEmpty() ? generateImportName(projectName) : name;
    }

    /**
     * @param projectId project id for searching candidates
     * @param datasetId dataset id (optional)
     * @return list of records which are candidates
     */
    public List<Record> getCandidates(String projectId, String datasetId) {
        Project project = projectsRepository.findByProjectId(projectId);
        if (datasetId == null) {
            return recordsRepository.findAllByProjectAndDatasetNullAndAnImportNull(project);
        } else {
            Dataset dataset = datasetsReposotory.findDatasetByDatasetId(datasetId);
            return recordsRepository.findAllByProjectAndDatasetAndAnImportNull(project, dataset);
        }
    }

    /**
     * Create import for project from given records
     *
     * @param name      name of the import, if null then creating using project name and date
     * @param projectId project id from which come records
     * @param records   list of records to assign to the import
     */
    public Import createImport(String name, String projectId, List<Record> records) {
        log.info("Creating import name {}, projectId {}, records {}", name, projectId, records);
        if (projectId == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Project project = projectsRepository.findByProjectId(projectId);
        Import anImport = Import.from(createImportName(name, project.getName()), records);
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
     * @param importName name of the import which should be send
     */
    public void sendExistingImport(String importName) throws NotFoundException {
        log.info("Sending existing import {}", importName);
        Import anImport = importsRepository.findImportByName(importName);
        if (anImport == null) {
            throw new NotFoundException("Import not found");
        }
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
        log.info("Getting content of import {}", inputImport);
        Import anImport = importsRepository.getOne(inputImport.getId());
        anImport.setRecords(recordsRepository.findAllByAnImport(anImport));
        return anImport;
    }

    /**
     * @param importName name of the import which status and failure should be returned
     * @return import status and import failure information
     */
    public ImportReport getStatusWithFailure(String importName) throws NotFoundException {
        log.info("Getting status with failure {}", importName);
        Import anImport = importsRepository.findImportByName(importName);
        if (anImport == null) {
            log.error("Empty import name for getting import status");
            throw new NotFoundException("Import not found");
        }
        return ImportReport.from(anImport.getStatus(), anImport.getFailures());
    }
}
