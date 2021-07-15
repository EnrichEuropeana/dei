package pl.psnc.dei.ui.components.batches;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.exception.ParseRecordsException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.ui.components.CommonComponentsFactory;
import pl.psnc.dei.util.EuropeanaRecordIdValidator;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BatchImportComponent extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(BatchImportComponent.class);

    private ProjectsRepository projectsRepository;
    private BatchService batchService;
    private ImportPackageService importPackageService;

    private Select<Aggregator> aggregatorsSelect;
    private Select<Project> projectsSelect;
    private Select<Dataset> datasetsSelect;
    private TextField importNameTextField;
    private TextArea recordsTextArea;

    private Aggregator aggregator;
    private Project project;
    private Dataset dataset;

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
        pageContainer.add(prepareImportNameInputField());
        pageContainer.add(new Label("Records:"));
        add(pageContainer);
        add(prepareRecordsTextArea());
        add(prepareImportButton());
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

    private Button prepareImportButton() {
        Button button = new Button("Import");
        button.addClickListener(e -> sendImport());
        return button;
    }

    private Component createAggregatorSelection() {
        Predicate<Aggregator> predicate = agg -> agg.equals(Aggregator.EUROPEANA);
        aggregatorsSelect = CommonComponentsFactory.getAggregatorSelector(predicate);
        aggregatorsSelect.setEnabled(false);
        aggregatorsSelect.setValue(Aggregator.EUROPEANA);
        return aggregatorsSelect;
    }

    private Component createProjectSelection() {
        projectsSelect = CommonComponentsFactory.getProjectSelector(projectsRepository);
        projectsSelect.addValueChangeListener(event -> {
            project = event.getValue();
            datasetsSelect.setItems(batchService.getProjectDataset(project));
        });
        return projectsSelect;
    }

    private Component createDatasetsSelection() {
        datasetsSelect = CommonComponentsFactory.getDatasetSelector(Collections.emptyList());
        datasetsSelect.addValueChangeListener(event -> dataset = event.getValue());
        return datasetsSelect;
    }

    private Component createImportNameInputField() {
        importNameTextField = new TextField();
        importNameTextField.addClassName("flex-1");
        return importNameTextField;
    }

    private TextArea prepareRecordsTextArea() {
        recordsTextArea = new TextArea();
        recordsTextArea.setMinHeight("150px");
        recordsTextArea.setWidthFull();
        recordsTextArea.addClassName("query-form");
        return recordsTextArea;
    }

    private void sendImport() {
        if (!areInputsValid()) {
            return;
        }
        Set<String> retrievedRecords;
        try {
            retrievedRecords = retrieveRecords();
        } catch (ParseRecordsException ex) {
            showNotification(ex.getMessage());
            return;
        }
        try {
            String datasetId = dataset != null ? dataset.getDatasetId() : null;
            Set<Record> uploadedRecords = batchService.uploadRecords(project.getName(), datasetId, retrievedRecords);
            if (uploadedRecords.isEmpty()) {
                showNotification("There are no records to upload!");
                return;
            }
            Import anImport = importPackageService.createImport(importNameTextField.getValue(), project.getProjectId(), uploadedRecords);
            showNotification("Successfully created import: " + anImport.getName());
        } catch (NotFoundException e) {
            showNotification("Invalid input. " + e.getMessage());
        }
    }

    private Set<String> retrieveRecords() throws ParseRecordsException {
        String rawInput = recordsTextArea.getValue();
        ObjectMapper mapper = new ObjectMapper();
        Set<String> parsedRecords;
        try {
            parsedRecords = mapper.readValue(rawInput, new TypeReference<Set<String>>() {
            });
        } catch (JsonParseException | JsonMappingException jsonProcessingException) {
            parsedRecords = readLines(rawInput);
        } catch (Exception e) {
            logger.error("Unable to retrieve input records. " + e.getMessage());
            throw new ParseRecordsException("Cannot read records!");
        }
        Set<String> normalizedRecords = normalizeRecords(parsedRecords);
        normalizedRecords.forEach(record -> {
            if (!isValidRecord(record)) {
                throw new ParseRecordsException("Invalid record: " + record);
            }
        });
        return normalizedRecords;
    }

    private Set<String> readLines(String input) {
        String[] split = input.split("\n");
        return Set.of(split);
    }

    private Set<String> normalizeRecords(Set<String> records) {
        return records.stream()
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private boolean isValidRecord(String s) {
        return EuropeanaRecordIdValidator.validate(s);
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
