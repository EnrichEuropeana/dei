package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.ImportFailure;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.ui.pages.ImportPage;

import java.util.Iterator;
import java.util.List;

/**
 * Creates list of all available imports grid;
 */
public class ImportsListGenerator {

    private static final int MAX_FAILURE_MESSAGE_SIZE = 30;
    private static final int MAX_FAILURES_DISPLAYED = 4;
    private static final String MORE_MARKER = "...";

    private final List<Import> imports;
    private final ImportPage importPage;

    public ImportsListGenerator(List<Import> imports, ImportPage importPage){
        this.imports = imports;
        this.importPage = importPage;
    }

    public Grid<Import> generate() {
        Grid<Import> importsGrid = new Grid<>();
        importsGrid.setMaxWidth("70%");

        importsGrid.addItemDoubleClickListener(e -> {
            Import imp = e.getItem();
            importPage.editImport(imp);
        });

        //
        ListDataProvider<Import> dataProvider = new ListDataProvider<>(
                imports);
        importsGrid.setDataProvider(dataProvider);

        Grid.Column<Import> projectColumn = importsGrid.addColumn(this::getProjectNameFromImport).setHeader("Project").setSortable(true).setFlexGrow(10);
        Grid.Column<Import> importNameColumn = importsGrid.addColumn(Import::getName).setHeader("Name").setSortable(true).setFlexGrow(10);
        Grid.Column<Import> creationDateColumn = importsGrid.addColumn(Import::getCreationDate).setHeader("Creation date").setSortable(true).setFlexGrow(10);
        Grid.Column<Import> statusColumn = importsGrid.addColumn(Import::getStatus).setHeader("Status").setSortable(true).setFlexGrow(5);
        importsGrid.addColumn(new ComponentRenderer<>(importInfo -> {
            String result = "<div>";
            Iterator<ImportFailure> iterator = importInfo.getFailures().iterator();
            int counter = 0;
            while (iterator.hasNext()) {
                counter++;
                if (counter > MAX_FAILURES_DISPLAYED) {
                    result += MORE_MARKER;
                    break;
                }
                ImportFailure importFailure = iterator.next();
                String reason = importFailure.getReason();
                if (reason.length() > MAX_FAILURE_MESSAGE_SIZE) {
                    reason = reason.substring(0, MAX_FAILURE_MESSAGE_SIZE) + MORE_MARKER;
                }
                result += "<p>" + reason + "</p>";

            }
            result += "</div>";
            return new Html(result);
        })).setHeader("Failures").setFlexGrow(10);

        //
        HeaderRow filterRow = importsGrid.appendHeaderRow();
        addFilter(dataProvider, filterRow, projectColumn, projectFilter);
        addFilter(dataProvider, filterRow, importNameColumn, nameFilter);
        addFilter(dataProvider, filterRow, statusColumn, statusFilter);
        addFilter(dataProvider, filterRow, creationDateColumn, dateFilter);
        //
        importsGrid.setColumnReorderingAllowed(true);
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

    //
    private final FieldFilter projectFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(getProjectNameFromImport(currentImport), currentValue);
    private final FieldFilter nameFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getName(), currentValue);
    private final FieldFilter statusFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getStatus().toString(), currentValue);
    private final FieldFilter dateFilter = (currentImport, currentValue) -> StringUtils.containsIgnoreCase(currentImport.getCreationDate().toString(), currentValue);

    private final FieldFilter failuresFilter = (currentImport, currentValue) -> {
        for (ImportFailure importFailure : currentImport.getFailures()) {
            if (StringUtils.containsIgnoreCase(importFailure.getReason(), currentValue)) {
                return true;
            }
        }
        return false;
    };

    private String getProjectNameFromImport(Import imp) {
        Iterator<Record> iterator = imp.getRecords().iterator();
        if(iterator.hasNext())
            return iterator.next().getProject().getName();
        return "";
    }
}


interface FieldFilter {
    boolean filter(Import currentImport, String currentValue);
}
