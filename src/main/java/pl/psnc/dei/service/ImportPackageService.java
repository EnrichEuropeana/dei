package pl.psnc.dei.service;

import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.DatasetsReposotory;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.*;
import pl.psnc.dei.request.RestRequestExecutor;
import reactor.core.publisher.Mono;

import java.util.*;

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

    public ImportPackageService(WebClient.Builder weBuilder) {
        configure(weBuilder);
    }

    private static String createImportName(String name, String projectName) {
        return StringUtil.isNullOrEmpty(name) ? generateImportName(projectName) : name;
    }

    public void updateImport(Import updatedImport, Set<Record> records) {
        importsRepository.findById(updatedImport.getId()).ifPresent(oldImport -> {

            Set<Record> oldRecords = recordsRepository.findAllByAnImport(updatedImport);
            for(Record record: oldRecords){
                record.setAnImport(null);
                recordsRepository.save(record);
            }

            Set<Record> removed = new HashSet<>(oldRecords);
            removed.removeAll(records);

            Set<Record> addedToImport = new HashSet<>(records);
            addedToImport.removeAll(oldRecords);

            for(Record record: removed) {
                record.setAnImport(null);
                recordsRepository.save(record);
            }
            updateRecords(addedToImport, oldImport);
        });
    }


    /**
     * @param projectId project id for searching candidates
     * @param datasetId dataset id (optional)
     * @return list of records which are candidates
     */
    public Set<Record> getCandidates(String projectId, String datasetId) {
        Project project = projectsRepository.findByProjectId(projectId);
        if (datasetId == null) {
            return recordsRepository.findAllByProjectAndAnImportNull(project);
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
    public Import createImport(String name, String projectId, Set<Record> records) {
        log.info("Creating import name {}, projectId {}, records {}", name, projectId, records);
        if (projectId == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Project project = projectsRepository.findByProjectId(projectId);
        Import anImport = Import.from(createImportName(name, project.getName()), new Date());
        anImport.setStatus(ImportStatus.CREATED);
        Import savedImport = this.importsRepository.save(anImport);
        updateRecords(records, savedImport);
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
        webClient.post()
                .uri(urlBuilder.urlForSendingImport())
                .body(BodyInserters.fromObject(anImport))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> {
                    log.info("Failed to send import {} {}", clientResponse.statusCode(), clientResponse.rawStatusCode());
                    anImport.setStatus(ImportStatus.FAILED);
                    return Mono.empty();
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Failed to send import {} {}", clientResponse.statusCode(), clientResponse.rawStatusCode());
                    anImport.setStatus(ImportStatus.FAILED);
                    return Mono.empty();
                })
                .onStatus(HttpStatus::is2xxSuccessful, clientResponse -> {
                    log.info("Import send successfully {}", clientResponse.statusCode());
                    anImport.setStatus(ImportStatus.SENT);
                    return Mono.empty();
                })
                .bodyToMono(Object.class).block();
        importsRepository.save(anImport);
    }

    /**
     * @param importId object Import which content should be returned
     * @return import with records which belong to it
     */
    public Import getContentOfImport(Long importId) {
        log.info("Getting content of import {}", importId);
        Import anImport = importsRepository.getOne(importId);
        anImport.setRecords(new HashSet<>(recordsRepository.findAllByAnImport(anImport)));
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
        return ImportReport.from(anImport.getStatus(), new ArrayList<>(anImport.getFailures()));
    }

    public Import addRecordsToImport(String importName, Set<Record> records) throws NotFoundException {
        log.info("Adding records to import {}, records {}", importName, records);
        Import anImport = importsRepository.findImportByName(importName);
        if (anImport == null) {
            throw new NotFoundException("Import not found");
        }
        updateRecords(records, anImport);
        return anImport;
    }

    private void updateRecords(Set<Record> records, Import anImport) {
        records.forEach(record -> {
            record.setAnImport(anImport);
            recordsRepository.findById(record.getId()).ifPresent(r -> {
                recordsRepository.findByIdentifier(r.getIdentifier());
                r.setAnImport(anImport);
                recordsRepository.save(r);
            });
        });
    }
}
