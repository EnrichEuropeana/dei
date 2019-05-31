package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class RecordsProjectsAssignmentService {

	@Autowired
	private RecordsRepository recordsRepository;

	@Autowired
	private CurrentUserRecordSelection currentUserRecordSelection;

	public void saveSelectedRecords() {
		Project project = currentUserRecordSelection.getSelectedProject();
		Dataset dataset = currentUserRecordSelection.getSelectedDataSet();
		Set<String> recordIds = currentUserRecordSelection.getSelectedRecordIds();
		if (recordIds != null) {
			recordIds.forEach(recordId -> {
				if (recordsRepository.findByIdentifierAndProjectAndDataset(recordId, project, dataset) == null) {
					Record record = recordsRepository.findByIdentifierAndProject(recordId, project);
					if (record == null) {
						Record newRecord = new Record();
						newRecord.setIdentifier(recordId);
						newRecord.setProject(project);
						newRecord.setDataset(dataset);
						recordsRepository.save(newRecord);
					} else {
						//record is assigned to project but not to dataset yet
						record.setDataset(dataset);
						recordsRepository.save(record);
					}
				}
			});
		}
	}

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
	public Set<Record> getAssignedRecords(Dataset dataset) {
		return recordsRepository.findAllByDataset(dataset);
	}

	/**
	 * Return the list of records that are assigned to the specified project.
	 *
	 * @param project project to which the returned records are assigned
	 * @return list of records assigned to the given project
	 */
	public Set<Record> getAssignedRecords(Project project) {
		return recordsRepository.findAllByProject(project);
	}
}
