package pl.psnc.dei.ui.components.batches;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
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

    private UI ui;

    public ComplexBatchImportComponent(ProjectsRepository projectsRepository, BatchService batchService) {
        this.projectsRepository = projectsRepository;
        this.batchService = batchService;
        this.init();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.ui = attachEvent.getUI();
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
        this.file.getElement().addEventListener("upload-abort", (DomEventListener) domEvent -> {
            memoryBuffer = new MemoryBuffer();
            file.setReceiver(memoryBuffer);
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
        this.removeOldImportName();
        this.setImportName();
    }

    private void setImportName() {
        if (this.nameTextFiled.getValue().isBlank()) {
            this.nameTextFiled.setValue(ImportNameCreatorUtil.generateImportName(this.projectSelect.getValue().getName()));
        } else {
            this.nameTextFiled.setValue(
                    this.nameTextFiled.getValue().strip()
            );
        }
        // push methods are needed as this entire code is run on some event, thus Vaadin optimization will
        // submit all changes after method associated with event will end
        // keep in mind that in same function we process (possibly big) set of records to be downloaded and put into imports
        this.ui.push();
    }

    private void removeOldImportName() {
        // removes name only if match to generated by default
        // there could not be dirtiness check on each validation / send button click to determine if user has provided custom name
        // as after first default name generation name field will be marked as dirty - typed as user and we have
        // no way to determine if it was actually typed by user or generated by us
        // if we do not create some more compound flagging system
        if (ImportNameCreatorUtil.isMatchingImportTitlePattern(this.nameTextFiled.getValue())) {
            this.nameTextFiled.clear();
            this.ui.push();
        }
    }

    private void sendImport() {
        try {
            if (this.validate()) {
                this.prepareImportName();
                this.showNotificationWithPush("Import(s) creation started");
                String datasetName = this.datasetSelect.getValue() == null ? null : this.datasetSelect.getValue().getName();
                List<?> imported = this.batchService
                        .makeComplexImport(
                                this.memoryBuffer.getInputStream(),
                                this.nameTextFiled.getValue(),
                                this.projectSelect.getValue().getName(),
                                datasetName
                        );
                this.showNotificationWithPush("Import(s) creation ended! Added " + imported.size() + " imports");
            }
        } catch (IOException e) {
            this.showNotification("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (NotFoundException e) {
            this.showNotification("Invalid data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showNotification(String text) {
        Notification.show(text, 3000, Notification.Position.MIDDLE);
    }

    private void showNotificationWithPush(String text) {
        this.showNotification(text);
        this.ui.push();
    }
}