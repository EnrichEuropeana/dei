package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.imports.DefaultImportOptions;
import pl.psnc.dei.ui.components.imports.SelectedRecordsList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Page for import generation.
 * <p>
 * Created by pwozniak on 4/8/19
 */
@Route(value = "import", layout = MainView.class)
public class ImportPage extends VerticalLayout {

    private RecordsRepository recordsRepository;
    private ImportPackageService importService;
    /*private TasksQueueService tasksQueueService;*/ //TODO uncomment when merging with queue
    private DefaultImportOptions defaultImportOptions;
    private Project selectedProject;

    private CurrentUserRecordSelection currentUserRecordSelection;

    private List<Record> foundRecords = new ArrayList<>();
    private SelectedRecordsList selectedRecordsList;
    private Button importButton = new Button();

    public ImportPage(RecordsRepository repo, ImportPackageService importService,
                      TranscriptionPlatformService transcriptionPlatformService,
                      /*TasksQueueService tasksQueueService,*/ //TODO uncomment when merging with queue
                      CurrentUserRecordSelection currentUserRecordSelection) {
        this.recordsRepository = repo;
        this.importService = importService;
        /*this.tasksQueueService = tasksQueueService;*/ //TODO uncomment when merging with queue
        this.defaultImportOptions = new DefaultImportOptions(transcriptionPlatformService, new ProjectChangeListener(),
                new DatasetChangeListener());
        this.currentUserRecordSelection = currentUserRecordSelection;
        this.selectedRecordsList = new SelectedRecordsList(currentUserRecordSelection);
        this.defaultImportOptions.add(importButton);

        importButton.setText("Create import and send");
        importButton.addClickListener(e -> handleImport());

        add(defaultImportOptions);
        add(selectedRecordsList);
    }

    private void handleImport() {
        List<String> recordIds = currentUserRecordSelection.getSelectedRecordIdsForImport();
        List<Record> records = recordIds.stream()
                .map(i -> recordsRepository.findByIdentifier(i))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        String importName = defaultImportOptions.getImportName();
        importService.createImport(importName, selectedProject.getProjectId(), records);

        for (Record record : records) {
            /*record.setState(Record.RecordState.E_PENDING); //TODO uncomment when merging with queue
            recordsRepository.save(record);
            EnrichTask task = new EnrichTask(record);
            taskQueueService.addTaskToQueue(transcriptionTask);*/
        }
        currentUserRecordSelection.clearSelectedRecordsForImport();
    }

    class ProjectChangeListener implements HasValue.ValueChangeListener<HasValue.ValueChangeEvent<Project>> {

        @Override
        public void valueChanged(HasValue.ValueChangeEvent<Project> event) {
            Project project = event.getValue();
            defaultImportOptions.updateImportName(project.getName());
            foundRecords = recordsRepository.findAllByProjectAndDatasetNullAndAnImportNull(project);
            selectedRecordsList.update(foundRecords);
            selectedProject = project;
        }
    }

    class DatasetChangeListener implements HasValue.ValueChangeListener<HasValue.ValueChangeEvent<Dataset>> {

        @Override
        public void valueChanged(HasValue.ValueChangeEvent<Dataset> event) {
            Dataset selectedDataset = event.getValue();
            if (selectedDataset != null) {
                foundRecords = recordsRepository.findAllByProjectAndDatasetAndAnImportNull(selectedDataset.getProject(), selectedDataset);
                selectedRecordsList.update(foundRecords);
            } else {
                foundRecords = recordsRepository.findAllByProjectAndDatasetNullAndAnImportNull(selectedProject);
                selectedRecordsList.update(foundRecords);
            }
        }
    }
}


