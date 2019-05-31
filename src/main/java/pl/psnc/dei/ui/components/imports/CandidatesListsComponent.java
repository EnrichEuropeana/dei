package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.ImportPackageService;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class CandidatesListsComponent extends VerticalLayout {

	private final CreateImportComponent.FieldFilter recordIdField = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(currentRecord.getIdentifier(), currentValue);
	private final CreateImportComponent.FieldFilter recordDatasetField = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(getDatasetValue(currentRecord), currentValue);

	private ProjectsRepository projectsRepository;
	private ImportPackageService importPackageService;

	private Set<Record> records;

	private Grid<Record> recordsList;
	private Button deleteButton;
	private Project project;
	private Aggregator aggregator;

	public CandidatesListsComponent(ProjectsRepository projectsRepository, ImportPackageService importPackageService) {
		this.projectsRepository = projectsRepository;
		this.importPackageService = importPackageService;
		add(createSelectionBar());
	}

	private void refresh() {
		records = importPackageService.getCandidates(aggregator, project);
		if (recordsList != null) {
			remove(recordsList);
		}
		recordsList = generateGrid();
		add(recordsList);
		if (deleteButton != null) {
			remove(deleteButton);
		}
		deleteButton = generateDeleteButton();
		add(deleteButton);
	}

	private Button generateDeleteButton() {
		Button button = new Button("Remove selected records");
		button.addClickListener(e -> {
			importPackageService.removeRecordsFromCandidates(recordsList.getSelectedItems());
			Notification.show("Records removed", 3000, Notification.Position.TOP_CENTER);
			refresh();
		});
		return button;
	}

	private Grid<Record> generateGrid() {
		Grid<Record> recordsGrid = new Grid<>();

		ListDataProvider<Record> dataProvider = new ListDataProvider<>(records);
		recordsGrid.setDataProvider(dataProvider);
		recordsGrid.setSelectionMode(Grid.SelectionMode.MULTI);

		Grid.Column<Record> recordIdColumn = recordsGrid.addColumn(Record::getIdentifier).setHeader("Id").setSortable(true).setFlexGrow(10);
		Grid.Column<Record> datasetColumn = recordsGrid.addColumn(Record::getDataset).setHeader("Dataset").setSortable(true).setFlexGrow(10);

		HeaderRow filterRow = recordsGrid.appendHeaderRow();
		addFilter(dataProvider, filterRow, recordIdColumn, recordIdField);
		addFilter(dataProvider, filterRow, datasetColumn, recordDatasetField);

		recordsGrid.setColumnReorderingAllowed(true);
		return recordsGrid;
	}

	private String getDatasetValue(Record record) {
		return record.getDataset() != null ? record.getDataset().getName() : "";
	}

	private Component createSelectionBar() {
		HorizontalLayout bar = new HorizontalLayout();
		Select<Project> projectSelection = new Select<>();
		ListDataProvider<Project> listDataProvider = new ListDataProvider<>(projectsRepository.findAll());
		projectSelection.setDataProvider(listDataProvider);
		projectSelection.setLabel("Select project");
		projectSelection.addValueChangeListener(event -> {
			project = event.getValue();
		});

		Select<Aggregator> aggregatorSelect = new Select<>();
		ListDataProvider<Aggregator> aggregatorsDataProvider = new ListDataProvider<>(Arrays.stream(Aggregator.values()).filter(e -> !e.equals(Aggregator.UNKNOWN)).collect(Collectors.toList()));
		aggregatorSelect.setDataProvider(aggregatorsDataProvider);
		aggregatorSelect.setLabel("Select aggregator");
		aggregatorSelect.addValueChangeListener(event -> {
			aggregator = event.getValue();
		});

		Button filterCandidates = new Button("Filter candidates");
		filterCandidates.addClickListener(e->{refresh();});

		bar.add(projectSelection);
		bar.add(aggregatorSelect);
		bar.add(filterCandidates);
		bar.setAlignItems(Alignment.END);
		return bar;
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
}
