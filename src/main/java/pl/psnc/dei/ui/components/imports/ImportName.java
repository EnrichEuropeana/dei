package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.textfield.TextField;

import java.util.Date;

/**
 * Default Vaadin text field extended by the algorithm for title creation.
 *
 * <p>
 * Created by pwozniak on 4/10/19
 */
public class ImportName extends TextField {

    private static final String TITLE_PATTERN = "IMPORT_%s_%tFT%tT";

    public ImportName() {
        this.setPlaceholder("Select project first");
        this.setLabel("Import name");
        this.setWidth("350px");   //TODO if shouldn't be in here
    }

    @Override
    public void setValue(String value) {
        super.setValue(generateImportName(value));
    }

    private String generateImportName(String value) {
        return String.format(TITLE_PATTERN, value, new Date(), new Date());
    }
}
