package pl.psnc.dei.ui.components.imports;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.ImportProgress;
import pl.psnc.dei.model.ImportStatus;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.ui.pages.ImportPage;

import java.util.Comparator;
import java.util.List;

public class ImportsListComponent extends VerticalLayout {

	private static final Logger logger = LoggerFactory.getLogger(ImportsListComponent.class);

	//
	private final FieldFilter nameFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getName(), currentValue);
	private final FieldFilter statusFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getStatus().toString(), currentValue);
	private final FieldFilter dateFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getCreationDate().toString(), currentValue);
	private ImportsRepository importsRepository;
	private ImportPackageService importPackageService;
	private List<Import> imports;
	private ImportPage importPage;
	private Component importList;

	public ImportsListComponent(ImportsRepository importsRepository, ImportPackageService importPackageService, ImportPage importPage) {
		this.importsRepository = importsRepository;
		this.importPackageService = importPackageService;
		this.importPage = importPage;
		refreshImportList();
	}

	private void refreshImportList(){
		imports = importsRepository.findAll();
		if(importList != null){
			remove(importList);
		}
		importList = generate();
		add(importList);
	}

	public Grid<Import> generate() {
		Grid<Import> importsGrid = new Grid<>();
		importsGrid.setMaxWidth("70%");
		//
		imports.sort(Comparator.comparing(Import::getCreationDate).reversed());
		ListDataProvider<Import> dataProvider = new ListDataProvider<>(imports);
		importsGrid.setDataProvider(dataProvider);

		Grid.Column<Import> importNameColumn = importsGrid.addColumn(Import::getName)
				.setHeader("Name")
				.setSortable(true)
				.setFlexGrow(20);
		Grid.Column<Import> creationDateColumn = importsGrid.addColumn(Import::getCreationDate)
				.setHeader("Creation date")
				.setSortable(true)
				.setFlexGrow(10);
		Grid.Column<Import> statusColumn = importsGrid.addComponentColumn(this::addStatusComponent)
				.setHeader("Status")
				.setComparator(Comparator.comparing(Import::getStatus))
				.setSortable(true)
				.setFlexGrow(4);
		importsGrid.addComponentColumn(this::addActionButtons).setHeader("Action").setFlexGrow(4);
		//
		HeaderRow filterRow = importsGrid.appendHeaderRow();
		addFilter(dataProvider, filterRow, importNameColumn, nameFilter);
		addFilter(dataProvider, filterRow, statusColumn, statusFilter);
		addFilter(dataProvider, filterRow, creationDateColumn, dateFilter);
		//
		importsGrid.setColumnReorderingAllowed(true);
		importsGrid.sort(new GridSortOrderBuilder<Import>().thenDesc(creationDateColumn).build());

		return importsGrid;
	}

	private Component addActionButtons(Import anImport) {
		HorizontalLayout layout = new HorizontalLayout();
		Button sendImportButton = new Button(new Icon(VaadinIcon.ENVELOPE_OPEN));
		sendImportButton.addClickListener(click -> {
			try {
				importPackageService.sendExistingImport(anImport.getName());
				Notification.show("Sending import started", 3000, Notification.Position.TOP_CENTER);
			} catch (NotFoundException ex) {
				Notification.show("Something goes wrong", 3000, Notification.Position.TOP_CENTER);
				logger.error("Import not found!", ex);
			}
			refreshImportList();
		});
		sendImportButton.setEnabled(shouldShowSendButton(anImport));

		Button editImportButton = new Button(new Icon(VaadinIcon.EDIT));
		editImportButton.addClickListener(click -> {
			importPage.showEditImportView(anImport);
		});
		layout.add(editImportButton);
		layout.add(sendImportButton);
		return layout;
	}

	private Component addStatusComponent(Import anImport) {
		ImportStatus status = anImport.getStatus();
		if (ImportStatus.IN_PROGRESS.equals(status)) {
			ProgressBar progressBar = new ProgressBar();
			ImportProgress progress = anImport.getProgress();
			if (progress.getCompletedTasks() == 0) {
				progressBar.setIndeterminate(true);
			} else {
				progressBar.setIndeterminate(false);
				double calcProgress = progress.getCompletedTasks() / (double) progress.getEstimatedTasks();
				progressBar.setValue(calcProgress);
			}
			return progressBar;
		}
		return new Span(status.name());
	}

	private boolean shouldShowSendButton(Import anImport) {
		return anImport != null && (ImportStatus.CREATED.equals(anImport.getStatus()) || ImportStatus.FAILED.equals(anImport.getStatus()));
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
