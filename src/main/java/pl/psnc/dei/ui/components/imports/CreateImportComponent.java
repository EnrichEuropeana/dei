package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.ImportStatus;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.util.ImportNameCreatorUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateImportComponent extends VerticalLayout {

	private final CreateImportComponent.FieldFilter importIdFilter = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(currentRecord.getIdentifier(), currentValue);
	private final CreateImportComponent.FieldFilter datasetFilter = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(currentRecord.getProject().toString(), currentValue);

	private RecordsRepository recordsRepository;
	private ImportPackageService importPackageService;
	private ProjectsRepository projectsRepository;

	private Set<Record> allRecords;
	private Set<Record> selectedRecordsForImport;

	private Grid<Record> allRecordsGrid;
	private Grid<Record> selectedRecordsGrid;
	private Import anImport;

	private HorizontalLayout switchingTables;
	private Component actionButtons;

	private Input importName;
	private Project project;

	public CreateImportComponent(ImportPackageService importPackageService, RecordsRepository recordsRepository, ProjectsRepository projectsRepository) {
		this.importPackageService = importPackageService;
		this.recordsRepository = recordsRepository;
		this.allRecords = new HashSet<>();
		this.projectsRepository = projectsRepository;
		this.selectedRecordsForImport = new HashSet<>();
		createComponent();
	}

	public CreateImportComponent(ImportPackageService importPackageService, Import anImport, RecordsRepository recordsRepository, ProjectsRepository projectsRepository) {
		this.anImport = anImport;
		this.importPackageService = importPackageService;
		this.recordsRepository = recordsRepository;
		this.projectsRepository = projectsRepository;
		this.selectedRecordsForImport = recordsRepository.findAllByAnImport(anImport);
		if (!selectedRecordsForImport.isEmpty()) {
			Project project = selectedRecordsForImport.iterator().next().getProject();
			this.allRecords = recordsRepository.findAllByProjectAndAnImportNull(project);
		} else {
			this.projectsRepository = projectsRepository;
			this.selectedRecordsForImport = new HashSet<>();
			this.allRecords = new HashSet<>();
			this.anImport = null;
		}
		createComponent();
	}

	private Component createProjectSelection() {
		Select<Project> projectSelect = new Select<>();
		ListDataProvider<Project> listDataProvider = new ListDataProvider<>(projectsRepository.findAll());
		projectSelect.setDataProvider(listDataProvider);

		projectSelect.setEnabled(anImport == null);
		projectSelect.addValueChangeListener(event -> {
			Project project = (Project) event.getValue();
			this.project = project;
			allRecords = recordsRepository.findAllByProjectAndAnImportNull(project);
			selectedRecordsForImport = new HashSet<>();
			importName.setValue(ImportNameCreatorUtil.generateImportName(project.getName()));
			refresh();
		});
		HorizontalLayout projectSelectionLayout = new HorizontalLayout();
		projectSelectionLayout.add(new Label("Select project"));
		projectSelectionLayout.add(projectSelect);
		return projectSelectionLayout;
	}

	private void refresh() {
		if (switchingTables != null) {
			remove(switchingTables);
		}
		if (actionButtons != null) {
			remove(actionButtons);
		}
		switchingTables = new HorizontalLayout();
		switchingTables.setWidthFull();
		allRecordsGrid = generateRecordsGrid(allRecords);
		switchingTables.add(allRecordsGrid);
		switchingTables.add(generateSwitchingButtons());
		selectedRecordsGrid = generateRecordsGrid(selectedRecordsForImport);
		switchingTables.add(selectedRecordsGrid);
		add(switchingTables);
		actionButtons = generateActionButtons();
		add(actionButtons);
	}

	private void createComponent() {
		setWidthFull();
		if (anImport == null) {
			add(createProjectSelection());
			HorizontalLayout importNameLayout = new HorizontalLayout();
			importName = new Input();
			importNameLayout.add(new Label("Import name"));
			importNameLayout.add(importName);
			add(importNameLayout);

			HorizontalLayout statusLayout = new HorizontalLayout();
			statusLayout.add(new Label("Import status"));
			statusLayout.add(new Label(ImportStatus.NEW.toString()));
			add(statusLayout);
		} else {
			HorizontalLayout importNameLayout = new HorizontalLayout();
			importName = new Input();
			importName.setEnabled(false);
			importName.setValue(anImport.getName());
			importNameLayout.add(new Label("Import name"));
			importNameLayout.add(importName);
			add(importNameLayout);

			HorizontalLayout statusLayout = new HorizontalLayout();
			statusLayout.add(new Label("Import status"));
			statusLayout.add(new Label(anImport.getStatus().toString()));
			add(statusLayout);
		}
		refresh();
	}

	private Component generateSwitchingButtons() {
		VerticalLayout switchingButtons = new VerticalLayout();
		switchingButtons.setMaxWidth("100px");
		Button addToSelected = new Button(new Icon(VaadinIcon.ARROW_CIRCLE_RIGHT));
		addToSelected.addClickListener(e -> {
			List<Record> waitingForMovingToSelected = new ArrayList<>(allRecordsGrid.getSelectionModel().getSelectedItems());
			selectedRecordsForImport.addAll(waitingForMovingToSelected);
			allRecords.removeAll(waitingForMovingToSelected);
			refresh();
		});
		addToSelected.setEnabled(shouldBeReadOnly());
		switchingButtons.add(addToSelected);

		Button moveFromSelectedToAll = new Button(new Icon(VaadinIcon.ARROW_CIRCLE_LEFT));
		moveFromSelectedToAll.addClickListener(e -> {
			List<Record> waitingForMovingToAll = new ArrayList<>(selectedRecordsGrid.getSelectionModel().getSelectedItems());
			allRecords.addAll(waitingForMovingToAll);
			selectedRecordsForImport.removeAll(waitingForMovingToAll);
			refresh();
		});
		switchingButtons.add(moveFromSelectedToAll);
		moveFromSelectedToAll.setEnabled(shouldBeReadOnly());
		return switchingButtons;
	}

	private boolean shouldBeReadOnly() {
		return !(anImport != null && ImportStatus.SENT == anImport.getStatus());
	}

	private Component generateActionButtons() {
		HorizontalLayout actionButtons = new HorizontalLayout();

		Button createButton = new Button("Create");
		createButton.setEnabled(shouldShowCreateButton());
		createButton.addClickListener(e -> {
			if(selectedRecordsForImport.isEmpty()){
				Notification.show("Import cannot be empty");
				return;
			}
			importPackageService.createImport(importName.getValue(), project.getProjectId(), selectedRecordsForImport);
		});
		actionButtons.add(createButton);

		Button updateButton = new Button("Update");
		updateButton.setEnabled(shouldShowUpdateButton());
		updateButton.addClickListener(e -> {
			if(selectedRecordsForImport.isEmpty()){
				Notification.show("Import cannot be empty");
				return;
			}
			importPackageService.updateImport(anImport, selectedRecordsForImport);
		});
		actionButtons.add(updateButton);

		Button sendButton = new Button("Send");
		sendButton.setEnabled(shouldShowSendButton());
		sendButton.addClickListener(e -> {
			try {
				importPackageService.sendExistingImport(anImport.getName());
			} catch (NotFoundException ex) {
				Notification.show("Something goes wrong");
				ex.printStackTrace();
			}
		});
		actionButtons.add(sendButton);

		return actionButtons;
	}

	private boolean shouldShowSendButton() {
		return anImport != null && ImportStatus.CREATED.equals(anImport.getStatus());
	}

	private boolean shouldShowUpdateButton() {
		return anImport != null && !(ImportStatus.IN_PROGRESS.equals(anImport.getStatus()) || ImportStatus.SENT.equals(anImport.getStatus()));
	}

	private boolean shouldShowCreateButton() {
		return anImport == null;
	}

	private Grid<Record> generateRecordsGrid(Set<Record> records) {
		Grid<Record> recordsGrid = new Grid<>();
		recordsGrid.setWidthFull();
		ListDataProvider<Record> dataProvider = new ListDataProvider<>(records);
		recordsGrid.setDataProvider(dataProvider);

		Grid.Column<Record> importIdColumn = recordsGrid.addColumn(Record::getIdentifier).setHeader("id").setSortable(true).setFlexGrow(10);
		Grid.Column<Record> importDatasetColumn = recordsGrid.addColumn(Record::getDataset).setHeader("dataset").setSortable(true).setFlexGrow(10);

		recordsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
//
		HeaderRow filterRow = recordsGrid.appendHeaderRow();
		addFilter(dataProvider, filterRow, importIdColumn, importIdFilter);
		addFilter(dataProvider, filterRow, importDatasetColumn, datasetFilter);
		//
		recordsGrid.setColumnReorderingAllowed(true);
		return recordsGrid;
	}

	private void addFilter(ListDataProvider<Record> dataProvider, HeaderRow filterRow, Grid.Column<Record> columnName, CreateImportComponent.FieldFilter fieldFilter) {
		TextField filterField = new TextField();
		filterField.addValueChangeListener(event -> dataProvider.addFilter(
				currentImport -> fieldFilter.filter(currentImport, filterField.getValue())));
		filterField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(columnName).setComponent(filterField);
		filterField.setSizeFull();
		filterField.setPlaceholder("Filter");
	}

	interface FieldFilter {
		boolean filter(Record currentRecord, String currentValue);
	}
}
