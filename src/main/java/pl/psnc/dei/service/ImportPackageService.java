package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class ImportPackageService {

    private final String SPACE_SEPARATOR = " ";
    private final String UNDERSCORE_SEPARATOR = "_";

    private ImportsRepository importsRepository;
    private RecordsRepository recordsRepository;

    @Autowired
    public ImportPackageService(ImportsRepository importsRepository, RecordsRepository recordsRepository) {
        this.importsRepository = importsRepository;
        this.recordsRepository = recordsRepository;
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
        Import anImport = Import.from(getImportName(name, project.getName()), records);
        this.importsRepository.save(anImport);
        records.forEach(record -> {
            record.setAnImport(anImport);
            recordsRepository.save(record);
        });
        return anImport;
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

    public void sendExistingImport(Import inputImport) {
        Import anImport = importsRepository.getOne(inputImport.getId());
        //todo send
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

    private String getImportName(String name, String projectName) {
        return name.isEmpty() ? StringUtils.replace(projectName, SPACE_SEPARATOR, UNDERSCORE_SEPARATOR) + UNDERSCORE_SEPARATOR + getCurrentDate() : name;
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-ddKK:mm:ssZ").format(Date.from(Instant.now()));
    }

}
