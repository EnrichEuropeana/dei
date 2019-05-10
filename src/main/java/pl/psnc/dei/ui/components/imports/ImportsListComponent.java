package pl.psnc.dei.ui.components.imports;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.ui.pages.ImportPage;

import java.util.List;

public class ImportsListComponent extends VerticalLayout {

	//
	private final FieldFilter nameFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getName(), currentValue);
	private final FieldFilter statusFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getStatus().toString(), currentValue);
	private final FieldFilter dateFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getCreationDate().toString(), currentValue);
	private ImportsRepository importsRepository;
	private List<Import> imports;
	private ImportPage importPage;

	public ImportsListComponent(ImportsRepository importsRepository, ImportPage importPage) {
		this.importsRepository = importsRepository;
		imports = importsRepository.findAll();
		this.importPage = importPage;
		add(generate());
	}

	public Grid<Import> generate() {
		Grid<Import> importsGrid = new Grid<>();
		importsGrid.setMaxWidth("70%");
		//
		ListDataProvider<Import> dataProvider = new ListDataProvider<>(imports);
		importsGrid.setDataProvider(dataProvider);

		Grid.Column<Import> importNameColumn = importsGrid.addColumn(Import::getName).setHeader("Name").setSortable(true).setFlexGrow(10);
		Grid.Column<Import> creationDateColumn = importsGrid.addColumn(Import::getCreationDate).setHeader("Creation date").setSortable(true).setFlexGrow(10);
		Grid.Column<Import> statusColumn = importsGrid.addColumn(Import::getStatus).setHeader("Status").setSortable(true).setFlexGrow(5);
		importsGrid.addComponentColumn(e -> addActionButtons()).setHeader("Action").setFlexGrow(5);

		//
		HeaderRow filterRow = importsGrid.appendHeaderRow();
		addFilter(dataProvider, filterRow, importNameColumn, nameFilter);
		addFilter(dataProvider, filterRow, statusColumn, statusFilter);
		addFilter(dataProvider, filterRow, creationDateColumn, dateFilter);
		//
		importsGrid.setColumnReorderingAllowed(true);
		return importsGrid;
	}

	private Component addActionButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		Button sendImportButton = new Button(new Icon(VaadinIcon.ENVELOPE_OPEN));
		sendImportButton.addClickListener(click -> {
			Notification.show("Sending imports not implemented yet");
		});
		Button editImportButton = new Button(new Icon(VaadinIcon.EDIT));
		editImportButton.addClickListener(click -> {
			Notification.show("Editing imports not implemented yet");
		});
		layout.add(editImportButton);
		layout.add(sendImportButton);
		return layout;
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

	interface FieldFilter {
		boolean filter(Import currentImport, String currentValue);
	}


}
