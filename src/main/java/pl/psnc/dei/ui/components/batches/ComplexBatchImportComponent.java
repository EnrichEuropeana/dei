package pl.psnc.dei.ui.components.batches;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.DatasetsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.ui.components.CommonComponentsFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ComplexBatchImportComponent extends VerticalLayout {
    private final ProjectsRepository projectsRepository;
    private final DatasetsRepository datasetsRepository;
    private final BatchService batchService;

    private Select<Project> projectSelect;
    private Select<Aggregator> aggregatorSelect;
    private Select<Dataset> datasetSelect;
    private Button fileSelectButton;
    private Button importButton;
    private TextField nameTextFiled;

    private Upload file;
    private MemoryBuffer memoryBuffer;

    public ComplexBatchImportComponent(ProjectsRepository projectsRepository, DatasetsRepository datasetsRepository, BatchService batchService) {
        this.projectsRepository = projectsRepository;
        this.datasetsRepository = datasetsRepository;
        this.batchService = batchService;
        this.init();
    }

    private void init() {
        this.initSelects();
        this.initUploads();
        this.setupLayout();
    }

    private void setupLayout() {
        Div container = new Div();

        HorizontalLayout selects = new HorizontalLayout();
        selects.add(this.aggregatorSelect);
        selects.add(this.projectSelect);
        selects.add(this.datasetSelect);
        container.add(selects);

        HorizontalLayout importName = new HorizontalLayout();
        importName.addClassName("vertically-centered-row");
        importName.add(this.nameTextFiled);
        container.add(importName);

        add(container);

        HorizontalLayout uploads = new HorizontalLayout();
        uploads.add(this.file);
        add(uploads);

        add(this.importButton);
    }

    private void initSelects() {
        this.prepareProjectSelect();
        this.prepareDatasetSelect();
        this.prepareAggregatorSelect();
    }

    private void prepareAggregatorSelect() {
        this.aggregatorSelect = new Select<>();
        this.aggregatorSelect.setLabel("Select Aggregator");
        this.aggregatorSelect.setItems(Arrays.stream(Aggregator.values()).filter(a -> a != Aggregator.UNKNOWN));
        this.aggregatorSelect.setValue(Aggregator.getById(0));
        this.aggregatorSelect.setEmptySelectionAllowed(false);
    }

    private void prepareProjectSelect() {
        this.projectSelect = CommonComponentsFactory.getProjectSelector(this.projectsRepository);
        this.projectSelect.setEmptySelectionAllowed(false);
        this.projectSelect.addValueChangeListener(event -> {
            Project project = this.projectSelect.getValue();
            this.datasetSelect.setItems(this.datasetsRepository.findAllByProject(project));
        });
    }

    private void prepareDatasetSelect() {
        Project project = this.projectSelect.getValue();
        this.datasetSelect =
                CommonComponentsFactory.getDatasetSelector(
                        (List<Dataset>) this.datasetsRepository.findAllByProject(project)
                );
    }

    private void initUploads() {
        this.prepareNameTextFiled();
        this.prepareFileSelectButton();
        this.prepareImportFile();
        this.prepareImportButton();
    }

    private void prepareFileSelectButton() {
        this.fileSelectButton = new Button();
        this.fileSelectButton.setText("Upload");
        this.fileSelectButton.setIcon(new Icon(VaadinIcon.CLOUD_UPLOAD_O));
    }

    private void prepareImportFile() {
        this.memoryBuffer = new MemoryBuffer();
        this.file = new Upload(this.memoryBuffer);
        this.file.setMaxFiles(1);
        this.file.setAcceptedFileTypes(".csv");
        this.file.setDropAllowed(true);
        this.file.setUploadButton(this.fileSelectButton);
        this.file.addFileRejectedListener(
                event -> Notification.show("File rejected. Reason: " + event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
        );
    }

    private void prepareNameTextFiled() {
        this.nameTextFiled = new TextField();
        this.nameTextFiled.setLabel("Import name");
        this.nameTextFiled.addClassName("flex-1");
        this.nameTextFiled.setMinLength(1);
    }

    private void prepareImportButton() {
        this.importButton = new Button();
        this.importButton.setText("Import");
        this.importButton.addClickListener(
                event -> {
                    try {
                        if (this.validate()) {
                            this.batchService
                                    .makeComplexImport(
                                            this.memoryBuffer.getInputStream(),
                                            this.nameTextFiled.getValue(),
                                            this.projectSelect.getValue().getName(),
                                            this.datasetSelect.getValue().getName()
                                    );
                            Notification.show("Import Finished!", 3000, Notification.Position.MIDDLE);
                        }
                        else Notification.show("Missing or wrong input", 3000, Notification.Position.MIDDLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private Boolean validate() throws IOException {
        if (this.aggregatorSelect.getValue() == null) return false;
        else if (this.projectSelect.getValue() == null) return false;
        else if (this.nameTextFiled.getValue().equals("")) return false;
        else if (this.memoryBuffer.getInputStream().readAllBytes().length == 0) return false;
        else return this.datasetSelect.getValue() != null;
    }
}