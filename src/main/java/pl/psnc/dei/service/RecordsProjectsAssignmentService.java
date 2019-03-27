package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import java.util.List;
import java.util.Objects;

@Service
public class RecordsProjectsAssignmentService {

    @Autowired
    private RecordsRepository recordsRepository;

    /**
     * Assign records to the specified project and dataset if not null.
     *
     * @param records list of records that should be assigned to the specified project and dataset
     * @param project project to which the records will be assigned
     * @param dataset if not null the records will be assigned to this dataset
     */
    public void assignRecords(List<Record> records, Project project, Dataset dataset) {
        Objects.requireNonNull(records);
        Objects.requireNonNull(project);
        if (dataset != null && !project.equals(dataset.getProject())) {
            throw new IllegalArgumentException("Dataset " + dataset.getDatasetId() + " is not part of project " + project.getProjectId());
        }

        records.forEach(record -> {
            record.setProject(project);
            if (dataset != null) {
                record.setDataset(dataset);
            }
            recordsRepository.save(record);
        });
    }

    /**
     * Unassign records from the specified project. Records and project must not be null. If a record is not assigned to the
     * specific project the record is skipped. If unassignment is actually done the dataset assignment is also cleared.
     *
     * @param records list of records to unassign from project
     * @param project project from which the records will be unassigned
     */
    public void unassignRecords(List<Record> records, Project project) {
        Objects.requireNonNull(records);
        Objects.requireNonNull(project);

        records.forEach(record -> {
            if (project.equals(record.getProject())) {
                record.setProject(null);
                record.setDataset(null);
                recordsRepository.save(record);
            }
        });
    }

    /**
     * Unassign records from the specified dataset. Dataset and records must not be null. Dataset has to be associated with the same project
     * that the record is. When a record is not assigned to the specified dataset the record is skipped.
     *
     * @param records list of records to unassign from the dataset
     * @param dataset dataset from which the records will be unassigned
     */
    public void unassignRecords(List<Record> records, Dataset dataset) {
        Objects.requireNonNull(records);
        Objects.requireNonNull(dataset);
        Objects.requireNonNull(dataset.getProject());

        records.forEach(record -> {
            if (dataset.equals(record.getDataset()) && dataset.getProject().equals(record.getProject())) {
                record.setDataset(null);
                recordsRepository.save(record);
            }
        });
    }

    /**
     * Return the list of records that are assigned to the specified dataset.
     *
     * @param dataset dataset to which the returned records are assigned
     * @return list of records assigned to the given dataset
     */
    public List<Record> getAssignedRecords(Dataset dataset) {
        return recordsRepository.findAllByDataset(dataset);
    }

    /**
     * Return the list of records that are assigned to the specified project.
     *
     * @param project project to which the returned records are assigned
     * @return list of records assigned to the given project
     */
    public List<Record> getAssignedRecords(Project project) {
        return recordsRepository.findAllByProject(project);
    }
}
