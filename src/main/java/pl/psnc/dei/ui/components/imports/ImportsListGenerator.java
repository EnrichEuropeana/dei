package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.ImportFailure;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.ui.pages.ImportPage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Creates list of all available imports grid;
 */
public class ImportsListGenerator {

	private static final int MAX_FAILURES_DISPLAYED = 3;
	private static final String MORE_MARKER = "...";

	private final List<Import> imports;
	private final ImportPage importPage;

	public ImportsListGenerator(List<Import> imports, ImportPage importPage) {
		this.imports = imports;
		this.importPage = importPage;
	}

	public Grid<Import> generate() {
		Grid<Import> importsGrid = new Grid<>();

		importsGrid.addItemDoubleClickListener(e -> {
			Import imp = e.getItem();
			importPage.showEditImportView(imp);
		});

		imports.sort(Comparator.comparing(Import::getCreationDate).reversed());

		//
		ListDataProvider<Import> dataProvider = new ListDataProvider<>(
				imports);
		importsGrid.setDataProvider(dataProvider);

		Grid.Column<Import> projectColumn = importsGrid.addColumn(this::getProjectNameFromImport).setHeader("Project").setSortable(true).setFlexGrow(5);
		Grid.Column<Import> importNameColumn = importsGrid.addColumn(Import::getName).setHeader("Name").setSortable(true).setFlexGrow(30);
		Grid.Column<Import> creationDateColumn = importsGrid.addColumn(Import::getCreationDate).setHeader("Creation date").setSortable(true).setFlexGrow(10);
		Grid.Column<Import> statusColumn = importsGrid.addColumn(Import::getStatus).setHeader("Status").setSortable(true).setFlexGrow(3);
		importsGrid.addColumn(new ComponentRenderer<>(importInfo -> {
			StringBuilder result = new StringBuilder("<div style=\"overflow-x: scroll\">");
			Iterator<ImportFailure> iterator = importInfo.getFailures().iterator();
			int counter = 0;
			while (iterator.hasNext()) {
				counter++;
				if (counter > MAX_FAILURES_DISPLAYED) {
					result.append(MORE_MARKER);
					break;
				}
				ImportFailure importFailure = iterator.next();
				String reason = importFailure.getReason();
				result.append("<p>").append(reason).append("</p>");

			}
			result.append("</div>");
			return new Html(result.toString());
		})).setHeader("Failures").setFlexGrow(50);

		//
		HeaderRow filterRow = importsGrid.appendHeaderRow();
		addFilter(dataProvider, filterRow, projectColumn, projectFilter);
		addFilter(dataProvider, filterRow, importNameColumn, nameFilter);
		addFilter(dataProvider, filterRow, statusColumn, statusFilter);
		addDateRangeFilter(dataProvider, filterRow, creationDateColumn);
		//
		importsGrid.setColumnReorderingAllowed(true);
		importsGrid.sort(new GridSortOrderBuilder<Import>().thenAsc(creationDateColumn).build());

		return importsGrid;
	}

	private void addFilter(ListDataProvider<Import> dataProvider, HeaderRow filterRow, Grid.Column<Import> columnName, FieldFilter fieldFilter) {
		TextField filterField = new TextField();
		filterField.addValueChangeListener(event -> dataProvider.addFilter(
				currentImport -> fieldFilter.filter(currentImport, filterField.getValue())));
		filterField.setValueChangeMode(ValueChangeMode.EAGER);
		filterRow.getCell(columnName).setComponent(filterField);
		filterField.setSizeFull();
		filterField.setPlaceholder("Filter");
	}

	private void addDateRangeFilter(ListDataProvider<Import> dataProvider, HeaderRow filterRow, Grid.Column<Import> columnName) {
		HorizontalLayout dateRangeFilter = new HorizontalLayout();
		dateRangeFilter.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		DatePicker fromDatePicker = new DatePicker();
		dateRangeFilter.add(fromDatePicker);

		dateRangeFilter.add(new Label("-"));

		DatePicker toDatePicker = new DatePicker();
		dateRangeFilter.add(toDatePicker);

		HasValue.ValueChangeListener listener = e -> {
			dataProvider.addFilter(currentImport -> {
				LocalDate from = fromDatePicker.getValue();
				LocalDate to = toDatePicker.getValue();
				LocalDate importDate = LocalDate.parse(currentImport.getCreationDate().toString().split(" ")[0]);
				boolean result = true;
				if (from != null) {
					result = from.isBefore(importDate) || from.isEqual(importDate);
				}
				if (to != null) {
					result = result && to.isAfter(importDate) || to.isEqual(importDate);
				}
				return result;
			});
		};

		fromDatePicker.addValueChangeListener(listener);
		toDatePicker.addValueChangeListener(listener);

		filterRow.getCell(columnName).setComponent(dateRangeFilter);
		dateRangeFilter.setSizeFull();
		fromDatePicker.setMaxWidth("45%");
		toDatePicker.setMaxWidth("45%");
	}

	//
	private final FieldFilter projectFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(getProjectNameFromImport(currentImport), currentValue);
	private final FieldFilter nameFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getName(), currentValue);
	private final FieldFilter statusFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getStatus().toString(), currentValue);


	private String getProjectNameFromImport(Import imp) {
		Iterator<Record> iterator = imp.getRecords().iterator();
		if (iterator.hasNext())
			return iterator.next().getProject().getName();
		return "";
	}
}


interface FieldFilter {
	boolean filter(Import currentImport, String currentValue);
}
