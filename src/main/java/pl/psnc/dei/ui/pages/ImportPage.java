package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.imports.DefaultImportOptions;
import pl.psnc.dei.ui.components.imports.SelectedRecordsList;

import java.util.ArrayList;
import java.util.List;

/**
 * Page for import generation.
 * <p>
 * Created by pwozniak on 4/8/19
 */
@Route(value = "import", layout = MainView.class)
public class ImportPage extends VerticalLayout {

    private RecordsRepository recordsRepository;
    private DefaultImportOptions defaultImportOptions;
    private Project selectedProject;

    private List<Record> foundRecords = new ArrayList<>();
    private SelectedRecordsList selectedRecordsList = new SelectedRecordsList();

    public ImportPage(RecordsRepository repo,
                      TranscriptionPlatformService transcriptionPlatformService) {
        this.recordsRepository = repo;
        this.defaultImportOptions = new DefaultImportOptions(transcriptionPlatformService, new ProjectChangeListener(), new DatasetChangeListener());
        add(defaultImportOptions);
        add(selectedRecordsList);
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


