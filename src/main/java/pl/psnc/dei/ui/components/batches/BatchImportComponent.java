package pl.psnc.dei.ui.components.batches;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.exception.ParseRecordsException;
import pl.psnc.dei.model.*;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.ui.components.CommonComponentsFactory;
import pl.psnc.dei.util.InputRecordsParser;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class BatchImportComponent extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(BatchImportComponent.class);

    private final ProjectsRepository projectsRepository;
    private final BatchService batchService;
    private final ImportPackageService importPackageService;

    private Select<Aggregator> aggregatorsSelect;
    private Select<Project> projectsSelect;
    private Select<Dataset> datasetsSelect;
    private Checkbox createImportCheckbox;
    private TextField importNameTextField;
    private TextArea recordsTextArea;

    private Project project;
    private Dataset dataset;
    private boolean doCreateImport = true;

    public BatchImportComponent(ProjectsRepository projectsRepository, BatchService batchService,
                                ImportPackageService importPackageService) {
        this.projectsRepository = projectsRepository;
        this.batchService = batchService;
        this.importPackageService = importPackageService;
        initLayout();
    }

    private void initLayout() {
        Div pageContainer = new Div();
        pageContainer.setId("batch-import-component");
        pageContainer.add(prepareAggregatorProjectDatasetSelector());
        pageContainer.add(prepareCreateImportCheckbox());
        pageContainer.add(prepareImportNameInputField());
        pageContainer.add(new Label("Records:"));
        add(pageContainer);
        add(prepareRecordsTextArea());
        add(prepareUploadRecordsButton());
    }

    private HorizontalLayout prepareAggregatorProjectDatasetSelector() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(createAggregatorSelection());
        horizontalLayout.add(createProjectSelection());
        horizontalLayout.add(createDatasetsSelection());
        return horizontalLayout;
    }

    private HorizontalLayout prepareImportNameInputField() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addClassName("vertically-centered-row");
        horizontalLayout.add(new Label("Import name"));
        horizontalLayout.add(createImportNameInputField());
        return horizontalLayout;
    }

    private Button prepareUploadRecordsButton() {
        Button button = new Button("Upload records");
        button.addClickListener(e -> uploadRecords());
        return button;
    }

    private Checkbox prepareCreateImportCheckbox() {
        createImportCheckbox = new Checkbox();
        createImportCheckbox.setLabel("Create import");
        createImportCheckbox.setValue(doCreateImport);
        createImportCheckbox.addValueChangeListener(event -> {
            doCreateImport = event.getValue();
            importNameTextField.setEnabled(doCreateImport);
        });
        return createImportCheckbox;
    }

    private Component createAggregatorSelection() {
        Predicate<Aggregator> predicate = agg -> agg.equals(Aggregator.EUROPEANA);
        aggregatorsSelect = CommonComponentsFactory.getAggregatorSelector(predicate);
        aggregatorsSelect.setEnabled(false);
        aggregatorsSelect.setValue(Aggregator.EUROPEANA);
        return aggregatorsSelect;
    }

    private Component createProjectSelection() {
        ListDataProvider<Project> projectsProvider = new ListDataProvider<>(projectsRepository.findAll());
        projectsSelect = CommonComponentsFactory.getProjectSelector(projectsProvider);
        projectsSelect.addValueChangeListener(event -> {
            project = event.getValue();
            datasetsSelect.setItems(batchService.getProjectDataset(project));
        });
        return projectsSelect;
    }

    private Component createDatasetsSelection() {
        datasetsSelect = CommonComponentsFactory.getDatasetSelector(Collections.emptyList());
        datasetsSelect.addValueChangeListener(event -> dataset = event.getValue());
        datasetsSelect.setEmptySelectionAllowed(true);
        return datasetsSelect;
    }

    private Component createImportNameInputField() {
        importNameTextField = new TextField();
        importNameTextField.addClassName("flex-1");
        return importNameTextField;
    }



    private TextArea prepareRecordsTextArea() {
        recordsTextArea = new TextArea();
        recordsTextArea.addClassName("records-text-area");
        recordsTextArea.setWidthFull();
        recordsTextArea.addClassName("query-form");
        recordsTextArea.setHelperText("Enter each Europeana ID in separate line or put JSON structured array of record IDs.");
        return recordsTextArea;
    }

    private void uploadRecords() {
        if (!areInputsValid()) {
            return;
        }
        Set<String> retrievedRecords;
        try {
            retrievedRecords = InputRecordsParser.parseRecords(recordsTextArea.getValue());
        } catch (ParseRecordsException ex) {
            showNotification(ex.getMessage());
            return;
        }
        try {
            String datasetId = dataset != null ? dataset.getDatasetId() : null;
            Set<Record> uploadedRecords = batchService.uploadRecords(project.getName(), datasetId, retrievedRecords);
            if (uploadedRecords.isEmpty()) {
                showNotification("No records to upload, because all the records have already been uploaded.");
                return;
            }
            if (doCreateImport) {
                Import anImport = importPackageService.createImport(importNameTextField.getValue(), project.getProjectId(), uploadedRecords);
                showNotification("Successfully created import: " + anImport.getName());
            } else {
                showNotification("Uploaded " + uploadedRecords.size() + " records!");
            }
        } catch (NotFoundException e) {
            showNotification("Invalid input. " + e.getMessage());
        } catch (Exception e) {
            showNotification(e.getMessage());
        }
    }

    private boolean areInputsValid() {
        String records = recordsTextArea.getValue();
        if (records == null || records.isBlank()) {
            showNotification("No records specified!");
            return false;
        }
        if (project == null) {
            showNotification("No project specified!");
            return false;
        }
        return true;
    }

    private void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }
}
