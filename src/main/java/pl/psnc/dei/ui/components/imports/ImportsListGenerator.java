package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.ImportFailure;

import java.util.Iterator;
import java.util.List;

/**
 * Creates list of all available imports grid;
 */
public class ImportsListGenerator {

    private static final int MAX_FAILURE_MESSAGE_SIZE = 30;
    private static final int MAX_FAILURES_DISPLAYED = 4;
    private static final String MORE_MARKER = "...";

    public static Grid<Import> generate(List<Import> imports) {
        Grid<Import> importsGrid = new Grid<>();
        importsGrid.setMaxWidth("70%");
        //
        importsGrid.addColumn(Import::getId).setHeader("Identifier").setFlexGrow(1);
        importsGrid.addColumn(Import::getName).setHeader("Name").setFlexGrow(10);
        importsGrid.addColumn(Import::getCreationDate).setHeader("Creation date").setFlexGrow(10);
        importsGrid.addColumn(TemplateRenderer.<Import>of("[[item.records]]")
                .withProperty("records",
                        importInfo -> importInfo.getRecords().size()))
                .setHeader("Records").setFlexGrow(1);

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

        importsGrid.addColumn(Import::getStatus).setHeader("Status").setFlexGrow(5);

        importsGrid.setColumnReorderingAllowed(true);
        importsGrid.setItems(imports);
        return importsGrid;
    }
}
