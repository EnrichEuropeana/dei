package pl.psnc.dei.service;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.Import;

import java.util.List;

@Service
@Transactional
public class ImportsHistoryService {

    private ImportsRepository importsRepository;

    public ImportsHistoryService(ImportsRepository importsRepository) {
        this.importsRepository = importsRepository;
    }

    public List<Import> getAllImports() {
        List<Import> imports = this.importsRepository.findAll();
        for (Import im : imports) {
            Hibernate.initialize(im.getRecords());
            Hibernate.initialize(im.getFailures());
        }
        return imports;
    }
}
