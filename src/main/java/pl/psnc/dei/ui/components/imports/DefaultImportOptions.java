package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.ui.components.ProjectSelectionComponent;

/**
 * Groups:
 * <ul>
 * <li>import name component</li>
 * <li>project and dataset components</li>
 * </ul>
 * into one horizontally aligned component
 * <br/><br/>
 * Created by pwozniak on 4/10/19
 */
public class DefaultImportOptions extends HorizontalLayout {

    private TextField importName = new ImportName();

    public DefaultImportOptions(TranscriptionPlatformService transcriptionPlatformService,
                                HasValue.ValueChangeListener projectChangeListener,
                                HasValue.ValueChangeListener datasetChangeListener) {

        add(importName, new ProjectSelectionComponent(transcriptionPlatformService, projectChangeListener, datasetChangeListener));
    }

    public void updateImportName(String newImportName) {
        importName.setValue(newImportName);
    }

    public String getImportName() {
        return importName.getValue();
    }
}
