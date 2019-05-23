package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;

import java.util.Set;

public class CandidatesListsComponent extends VerticalLayout {

	private final CreateImportComponent.FieldFilter recordIdField = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(currentRecord.getIdentifier(), currentValue);
	private final CreateImportComponent.FieldFilter recordDatasetField = (currentRecord, currentValue) -> StringUtils.containsIgnoreCase(getDatasetValue(currentRecord), currentValue);

	private ProjectsRepository projectsRepository;
	private RecordsRepository recordsRepository;

	private Set<Record> records;

	private Grid<Record> recordsList;
	private Button deleteButton;
	private Project project;

	public CandidatesListsComponent(ProjectsRepository projectsRepository, RecordsRepository recordsRepository) {
		this.projectsRepository = projectsRepository;
		this.recordsRepository = recordsRepository;
		add(createProjectSelection());
	}

	private void refresh() {
		records = recordsRepository.findAllByProjectAndAnImportNull(project);
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
			for (Record record : recordsList.getSelectedItems()) {
				recordsRepository.delete(record);
			}
			refresh();
		});
		return button;
	}

	private Grid<Record> generateGrid() {
		Grid<Record> recordsGrid = new Grid<>();
		recordsGrid.setMaxWidth("70%");

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

	private String getDatasetValue(Record record){
		return record.getDataset() != null? record.getDataset().getName() : "";
	}

	private Component createProjectSelection() {
		Select<Project> projectSelect = new Select<>();
		ListDataProvider<Project> listDataProvider = new ListDataProvider<>(projectsRepository.findAll());
		projectSelect.setDataProvider(listDataProvider);

		projectSelect.addValueChangeListener(event -> {
			project = event.getValue();
			refresh();
		});
		HorizontalLayout projectSelectionLayout = new HorizontalLayout();
		projectSelectionLayout.add(new Label("Select project"));
		projectSelectionLayout.add(projectSelect);
		return projectSelectionLayout;
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
