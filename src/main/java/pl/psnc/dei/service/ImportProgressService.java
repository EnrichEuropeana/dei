package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.ImportProgress;
import pl.psnc.dei.model.Record;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class ImportProgressService {

    private final ImportsRepository importsRepository;

    public ImportProgressService(ImportsRepository importsRepository) {
        this.importsRepository = importsRepository;
    }

    public ImportProgress initImportProgress(int recordsToSend) {
        /*
         * Multiplied by 3, because each record can do at most 3 tasks:
         * fetch data from europeana
         * convert to iiif
         * send to transcribathon
         */
        ImportProgress importProgress = new ImportProgress();
        int estimatedTasks = recordsToSend * 3;
        importProgress.setEstimatedTasks(estimatedTasks);
        importProgress.setCompletedTasks(0);
        return importProgress;
    }

    public void reportProgress(Record record) {
        String importName = record.getAnImport().getName();
        Optional<Import> importOptional = importsRepository.findImportByName(importName);
        if (importOptional.isEmpty()) {
            throw new AssertionError("Import " + importName + " not found");
        }
        Import anImport = importOptional.get();
        anImport.getProgress().incrementCompleted();
        record.setAnImport(importsRepository.save(anImport));
    }
}
