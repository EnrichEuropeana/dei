package pl.psnc.dei.ui.components.batches;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.dei.controllers.requests.CreateImportFromDatasetRequest;
import pl.psnc.dei.controllers.requests.UploadDatasetRequest;
import pl.psnc.dei.exception.ParseRecordsException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.response.search.europeana.EuropeanaItem;
import pl.psnc.dei.response.search.europeana.EuropeanaSearchResponse;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.ui.components.CheckboxWithIntegerInput;
import pl.psnc.dei.ui.components.CommonComponentsFactory;
import pl.psnc.dei.util.InputRecordsParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static pl.psnc.dei.ui.pages.SearchPage.ONLY_IIIF_PARAM_NAME;

public class DatasetImportComponent extends VerticalLayout {

    //Injected beans
    private final ProjectsRepository projectsRepository;
    private final BatchService batchService;
    private final EuropeanaSearchService europeanaSearchService;

    //UI components
    private Select<Aggregator> aggregatorsSelect;
    private Select<Project> projectsSelect;
    private Select<Dataset> datasetsSelect;

    private TextField europeanaDatasetIdTextField;
    private AccordionPanel previewAccordion;

    private Checkbox limitRecordsToRetrieveCheckbox;
    private IntegerField limitRecordsToRetrieveIntegerField;

    private Checkbox excludeSelectedCheckbox;
    private TextArea excludeSelectedTextArea;

    private Checkbox createImportCheckbox;
    private TextField importNameTextField;

    private CheckboxWithIntegerInput splitRecordsCheckboxWithNumber;
    private Checkbox splitRecordsCheckbox;
    private IntegerField splitRecordsIntegerField;

    //Class fields
    private Project project;
    private Dataset dataset;
    private String europeanaDatasetId;
    private boolean doLimitNumberOfRecordsToRetrieve = false;
    private boolean doExcludeSelected = false;
    private boolean doCreateImport = true;
    private boolean doSplitRecords = false;

    //Constants
    private final String QUERY_FORMAT = "edm_datasetName:%s_*";

    public DatasetImportComponent(ProjectsRepository projectsRepository, BatchService batchService,
                                  EuropeanaSearchService europeanaSearchService) {
        this.projectsRepository = projectsRepository;
        this.batchService = batchService;
        this.europeanaSearchService = europeanaSearchService;
        initLayout();
    }

    private void initLayout() {
        Div pageContainer = new Div();
        pageContainer.addClassName("flex-column");
        pageContainer.add(prepareAggregatorProjectDatasetSelector());
        pageContainer.add(prepareDatasetNameInputField());
        pageContainer.add(preparePreviewAccordion());
        pageContainer.add(prepareRecordLimitInput());
        pageContainer.add(prepareExclusionCheckbox());
        pageContainer.add(prepareExcludedTextArea());
        pageContainer.add(prepareCreateImportCheckbox());
        pageContainer.add(prepareImportNameInputField());
        pageContainer.add(prepareRecordsSplittingInput());
        add(pageContainer);
        add(prepareUploadRecordsButton());
    }

    private Component prepareExclusionCheckbox() {
        excludeSelectedCheckbox = new Checkbox();
        excludeSelectedCheckbox.setLabel("Exclude following records");
        excludeSelectedCheckbox.addValueChangeListener(event -> {
            doExcludeSelected = event.getValue();
            excludeSelectedTextArea.setEnabled(doExcludeSelected);
        });
        return excludeSelectedCheckbox;
    }

    private HorizontalLayout prepareAggregatorProjectDatasetSelector() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(createAggregatorSelection());
        horizontalLayout.add(createProjectSelection());
        horizontalLayout.add(createDatasetsSelection());
        return horizontalLayout;
    }

    private Component prepareDatasetNameInputField() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        europeanaDatasetIdTextField = new TextField();
        europeanaDatasetIdTextField.addClassName("flex-1");
        europeanaDatasetIdTextField.setLabel("Europeana Dataset ID");
        europeanaDatasetIdTextField.addValueChangeListener(event ->
                europeanaDatasetId = event.getValue()
        );
        Button button = new Button();
        button.setText("Check dataset");
        button.addClassName("align-flex-end");
        button.addClickListener(event -> checkDataset());
        horizontalLayout.add(europeanaDatasetIdTextField, button);
        return horizontalLayout;
    }

    private Component prepareRecordLimitInput() {
        CheckboxWithIntegerInput checkbox = new CheckboxWithIntegerInput("Limit number of records to retrieve to");
        limitRecordsToRetrieveCheckbox = checkbox.getCheckbox();
        limitRecordsToRetrieveIntegerField = checkbox.getIntegerField();
        limitRecordsToRetrieveIntegerField.setEnabled(doLimitNumberOfRecordsToRetrieve);
        limitRecordsToRetrieveCheckbox.addValueChangeListener(event -> {
            doLimitNumberOfRecordsToRetrieve = event.getValue();
            limitRecordsToRetrieveIntegerField.setEnabled(doLimitNumberOfRecordsToRetrieve);
        });
        return checkbox;
    }

    private Component prepareRecordsSplittingInput() {
        splitRecordsCheckboxWithNumber = new CheckboxWithIntegerInput("Split records into several imports with a maximum size of");
        splitRecordsCheckboxWithNumber.setEnabled(doCreateImport);
        splitRecordsCheckbox = splitRecordsCheckboxWithNumber.getCheckbox();
        splitRecordsIntegerField = splitRecordsCheckboxWithNumber.getIntegerField();
        splitRecordsIntegerField.setEnabled(doSplitRecords);
        splitRecordsCheckbox.addValueChangeListener(event -> {
            doSplitRecords = event.getValue();
            splitRecordsIntegerField.setEnabled(doSplitRecords);
        });
        return splitRecordsCheckboxWithNumber;
    }

    private TextField prepareImportNameInputField() {
        importNameTextField = new TextField();
        importNameTextField.setLabel("Import name");
        importNameTextField.addClassName("padding-top-6px");
        return importNameTextField;
    }

    private Button prepareUploadRecordsButton() {
        Button button = new Button("Upload records");
        button.addClickListener(event -> uploadRecords());
        return button;
    }

    private Checkbox prepareCreateImportCheckbox() {
        createImportCheckbox = new Checkbox();
        createImportCheckbox.setLabel("Create import");
        createImportCheckbox.setValue(doCreateImport);
        createImportCheckbox.addClassName("margin-top-16px");
        createImportCheckbox.addValueChangeListener(event -> {
            doCreateImport = event.getValue();
            importNameTextField.setEnabled(doCreateImport);
            splitRecordsCheckboxWithNumber.setEnabled(doCreateImport);
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

    private TextArea prepareExcludedTextArea() {
        excludeSelectedTextArea = new TextArea();
        excludeSelectedTextArea.addClassName("flex-1");
        excludeSelectedTextArea.addClassName("records-text-area");
        excludeSelectedTextArea.setHelperText("Enter each Europeana ID in separate line or put JSON structured array of record IDs.");
        excludeSelectedTextArea.setEnabled(doExcludeSelected);
        return excludeSelectedTextArea;
    }

    private AccordionPanel preparePreviewAccordion() {
        previewAccordion = new AccordionPanel(getEmptyDatasetPreviewHeader(), null);
        previewAccordion.setEnabled(false);
        return previewAccordion;
    }

    private void fillAccordion(String datasetName, int totalRows, List<RecordSample> recordSamples) {
        previewAccordion.setSummaryText(getDatasetPreviewHeader(datasetName, totalRows));
        previewAccordion.setContent(preparePreviewRecordsGrid(recordSamples));
        previewAccordion.setEnabled(true);
    }

    private Grid<RecordSample> preparePreviewRecordsGrid(List<RecordSample> recordSamples) {
        Grid<RecordSample> grid = new Grid<>();
        grid.setThemeName("compact");
        grid.setHeightByRows(true);
        grid.addColumn(RecordSample::getId).setHeader("Record ID");
        grid.addColumn(RecordSample::getTitle).setHeader("Title");
        grid.setItems(recordSamples);
        return grid;
    }

    private void uploadRecords() {
        if (!validateInputs()) {
            return;
        }
        Set<String> excludedRecords = null;
        try {
            if (doExcludeSelected) {
                excludedRecords = InputRecordsParser.parseRecords(excludeSelectedTextArea.getValue());
            }
        } catch (ParseRecordsException pre) {
            showNotification(pre.getMessage());
            return;
        }
        if (doCreateImport) {
            createImportFromDataset(excludedRecords);
        } else {
            uploadDatasetRecords(excludedRecords);
        }
    }

    private CreateImportFromDatasetRequest prepareRequest(Set<String> excludedRecords) {
        CreateImportFromDatasetRequest request = new CreateImportFromDatasetRequest();
        request.setProjectName(project.getName());
        request.setDataset(dataset == null ? null : dataset.getName());
        request.setEuropeanaDatasetId(europeanaDatasetId);
        if (doLimitNumberOfRecordsToRetrieve) {
            request.setLimit(limitRecordsToRetrieveIntegerField.getValue());
        }
        if (excludedRecords != null) {
            request.setExcludedRecords(excludedRecords);
        }
        if (doCreateImport) {
            request.setImportName(importNameTextField.getValue());
        }
        if (doSplitRecords) {
            request.setImportSize(splitRecordsIntegerField.getValue());
        }
        return request;
    }

    private void uploadDatasetRecords(Set<String> excludedRecords) {
        UploadDatasetRequest request = prepareRequest(excludedRecords);
        try {
            Set<Record> records = batchService.uploadDataset(request);
            if (records.isEmpty()) {
                showNotification("No records found to upload!");
            } else {
                showNotification("Successfully uploaded " + records.size() + " records from Dataset ID"  + europeanaDatasetId);
            }
        } catch (Exception e) {
            showNotification("Error: " + e.getMessage());
        }
    }

    private void createImportFromDataset(Set<String> excludedRecords) {
        CreateImportFromDatasetRequest request = prepareRequest(excludedRecords);
        try {
            List<Import> imports = batchService.createImportsFromDataset(request);
            if (imports.isEmpty()) {
                showNotification("No records found for creating an import!");
            } else {
                showNotification(prepareSuccessNotificationMessage(imports));
            }
        } catch (Exception e) {
            showNotification("Error: " + e.getMessage());
        }
    }

    private String prepareSuccessNotificationMessage(List<Import> imports) {
        StringBuilder messageBuilder = new StringBuilder("Successfully created " + imports.size() + " import(s) from Dataset ID" + europeanaDatasetId);
        imports.forEach(anImport -> messageBuilder.append("\n").append(anImport.getName()));
        return messageBuilder.toString();
    }

    private boolean validateInputs() {
        if (europeanaDatasetId == null || europeanaDatasetId.isBlank()) {
            showNotification("No Europeana Dataset ID specified!");
            return false;
        }
        if (project == null) {
            showNotification("No project specified");
            return false;
        }
        if (doLimitNumberOfRecordsToRetrieve && limitRecordsToRetrieveIntegerField.isEmpty()) {
            showNotification("Missing number of records to retrieve!");
            return false;
        }
        if (doSplitRecords && splitRecordsIntegerField.isEmpty()) {
            showNotification("Missing size of records to split!");
            return false;
        }
        return true;
    }

    private void checkDataset() {
        EuropeanaSearchResponse searchResponse = searchForDataset();
        if (searchResponse.getItems().isEmpty() || Boolean.FALSE.equals(searchResponse.getSuccess())) {
            showNotification("Cannot find dataset with given ID");
            return;
        }
        String datasetName = extractDatasetName(searchResponse.getItems().get(0));
        int totalRecords = searchResponse.getTotalResults();
        List<RecordSample> recordSamples = searchResponse.getItems().stream()
                .map(RecordSample::from)
                .collect(Collectors.toList());
        fillAccordion(datasetName, totalRecords, recordSamples);
    }

    private String extractDatasetName(EuropeanaItem item) {
        return item.getEdmDatasetName().get(0);
    }

    private String getEmptyDatasetPreviewHeader() {
        return getDatasetPreviewHeader("Dataset", 0);
    }

    private String getDatasetPreviewHeader(String datasetName, int totalRows) {
        return String.format("%s preview (%d records)", datasetName, totalRows);
    }

    private EuropeanaSearchResponse searchForDataset() {
        String query = String.format(QUERY_FORMAT, europeanaDatasetId);
        Map<String, String> requestParams = ImmutableMap.of(
                ONLY_IIIF_PARAM_NAME, "false"
        );
        return (EuropeanaSearchResponse) europeanaSearchService.search(query, requestParams, 5).block();
    }

    private void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RecordSample {
        private String id;
        private String title;

        public static RecordSample from(EuropeanaItem item) {
            RecordSample recordSample = new RecordSample();
            recordSample.setId(item.getId());
            recordSample.setTitle(item.getTitle().get(0));
            return recordSample;
        }
    }
}
