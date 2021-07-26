package pl.psnc.dei.ui.components.batches;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.ui.components.CommonComponentsFactory;
import pl.psnc.dei.util.ImportNameCreatorUtil;

import java.io.IOException;
import java.util.List;

public class ComplexBatchImportComponent extends VerticalLayout {
    private final ProjectsRepository projectsRepository;
    private final BatchService batchService;

    private Select<Project> projectSelect;
    private Select<Aggregator> aggregatorSelect;
    private Select<Dataset> datasetSelect;
    private Button fileSelectButton;
    private Button importButton;
    private TextField nameTextFiled;

    private Upload file;
    private MemoryBuffer memoryBuffer;

    public ComplexBatchImportComponent(ProjectsRepository projectsRepository, BatchService batchService) {
        this.projectsRepository = projectsRepository;
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

        HorizontalLayout uploads = new HorizontalLayout();
        uploads.addClassName("vertically-centered-row");
        uploads.add(this.file);
        container.add(uploads);

        add(container);

        add(this.importButton);
    }

    private void initSelects() {
        this.prepareProjectSelect();
        this.prepareDatasetSelect();
        this.prepareAggregatorSelect();
    }

    private void prepareAggregatorSelect() {
        this.aggregatorSelect = CommonComponentsFactory.getAggregatorSelector();
        this.aggregatorSelect.setValue(Aggregator.getById(0));
        this.aggregatorSelect.setEmptySelectionAllowed(false);
        this.aggregatorSelect.setValue(Aggregator.EUROPEANA);
        this.aggregatorSelect.setEnabled(false);
    }

    private void prepareProjectSelect() {
        ListDataProvider<Project> projectsProvider = new ListDataProvider<>(this.projectsRepository.findAll());
        this.projectSelect = CommonComponentsFactory.getProjectSelector(projectsProvider);
        this.projectSelect.setEmptySelectionAllowed(false);
        this.projectSelect.setValue(projectsProvider.getItems().stream().findFirst().orElseGet(null));
        this.projectSelect.addValueChangeListener(event -> {
            Project project = this.projectSelect.getValue();
            this.datasetSelect.setItems(this.batchService.getProjectDataset(project));
        });
    }

    private void prepareDatasetSelect() {
        Project project = this.projectSelect.getValue();
        this.datasetSelect =
                CommonComponentsFactory.getDatasetSelector(
                    this.batchService.getProjectDataset(project)
                );
        this.datasetSelect.setEmptySelectionAllowed(true);
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
        this.file.getElement().addEventListener("upload-abort", new DomEventListener() {
            @Override
            public void handleEvent(DomEvent domEvent) {
                memoryBuffer = new MemoryBuffer();
                file.setReceiver(memoryBuffer);
            }
        });
        this.file.setDropLabel(new Label("Upload up to one file to make import from"));
        this.file.addClassName("flex-1");
        this.file.addClassName("margin-top-16px");
    }

    private void prepareNameTextFiled() {
        this.nameTextFiled = new TextField();
        this.nameTextFiled.setLabel("Import name");
        this.nameTextFiled.addClassName("flex-1");
    }

    private void prepareImportButton() {
        this.importButton = new Button();
        this.importButton.setText("Import");
        this.importButton.addClickListener(
                event -> this.sendImport());
    }

    private Boolean validate() throws IOException {
        if (this.aggregatorSelect.getValue() == null){
            Notification.show("Select Aggregator", 3000, Notification.Position.MIDDLE);
            return false;
        } else if (this.projectSelect.getValue() == null) {
            Notification.show("Select Project", 3000, Notification.Position.MIDDLE);
            return false;
        } else if (this.memoryBuffer.getInputStream().readAllBytes().length == 0) {
            Notification.show("Select File", 3000, Notification.Position.MIDDLE);
            return false;
        }
        return true;
    }

    private void prepareImportName() {
        if (this.nameTextFiled.getValue().isBlank()) {
            this.nameTextFiled.setValue(ImportNameCreatorUtil.generateImportName(this.projectSelect.getValue().getName()));
        } else {
            this.nameTextFiled.setValue(
                    this.nameTextFiled.getValue().strip()
            );
        }
    }

    private void sendImport() {
        try {
            this.prepareImportName();
            if (this.validate()) {
                String datasetName = this.datasetSelect.getValue() == null ? null : this.datasetSelect.getValue().getName();
                List<?> imported = this.batchService
                        .makeComplexImport(
                                this.memoryBuffer.getInputStream(),
                                this.nameTextFiled.getValue(),
                                this.projectSelect.getValue().getName(),
                                datasetName
                        );
                Notification.show("Import Finished! Added " + imported.size() + " imports", 3000, Notification.Position.MIDDLE);
            }
        } catch (IOException e) {
            Notification.show("IOException: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            e.printStackTrace();
        } catch (NotFoundException e) {
            Notification.show("Invalid data: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }
}