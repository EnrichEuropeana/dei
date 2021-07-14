package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.ui.components.CommonComponentsFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CandidatesListsComponent extends VerticalLayout {

	private final CreateImportComponent.FieldFilter recordIdField = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(currentRecord.getTitle(), currentValue);
	private final CreateImportComponent.FieldFilter recordDatasetField = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(getDatasetValue(currentRecord), currentValue);

	private final ProjectsRepository projectsRepository;
	private final ImportPackageService importPackageService;

	private Select<Aggregator> aggregators;
	private Select<Project> projects;

	private Set<Record> records;

	private Grid<Record> recordsList;
	private Button deleteButton;
	private Project project;
	private Aggregator aggregator;

	public CandidatesListsComponent(ProjectsRepository projectsRepository, ImportPackageService importPackageService) {
		this.projectsRepository = projectsRepository;
		this.importPackageService = importPackageService;
		add(createSelectionBar());
		refresh();
	}

	private void refresh() {
		if (aggregator == null | project == null) {
			records = new HashSet<>();
		} else {
			records = importPackageService.getCandidates(aggregator, project);
		}
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
		button.setEnabled(false);
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
		recordsGrid.addSelectionListener((SelectionListener<Grid<Record>, Record>) selectionEvent ->
				deleteButton.setEnabled(!selectionEvent.getSource().getSelectedItems().isEmpty()));

		Grid.Column<Record> recordIdColumn = recordsGrid.addColumn(Record::getTitle).setHeader("Title").setSortable(true).setFlexGrow(10);
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

		bar.add(aggregatorSelector());
		bar.add(projectSelector());
		bar.setAlignItems(Alignment.END);
		return bar;
	}

	private Select<Aggregator> aggregatorSelector(){
		aggregators = CommonComponentsFactory.getAggregatorSelector();
		aggregators.addValueChangeListener(event -> {
			aggregator = event.getValue();
			projects.setReadOnly(false);
			refresh();
		});
		return aggregators;
	}

	private Select<Project> projectSelector(){
		projects = CommonComponentsFactory.getProjectSelector(projectsRepository);
		projects.setReadOnly(true);
		projects.addValueChangeListener(event -> {
			project = event.getValue();
			refresh();
		});
		return projects;
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
