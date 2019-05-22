
package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.model.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * Component for storing and displaying selected records.
 * <p>
 * Created by pwozniak on 4/10/19
 */
public class SelectedRecordsList extends VerticalLayout {

    private CurrentUserRecordSelection currentUserRecordSelection;

    private List<RecordForImportComponent> recordsForImport = new ArrayList<>();

    public SelectedRecordsList(CurrentUserRecordSelection currentUserRecordSelection) {
        this.currentUserRecordSelection = currentUserRecordSelection;
    }

    public void update(List<Record> records) {
        recordsForImport.clear();
        this.removeAll();

        for (Record r : records) {
            boolean isSelected = currentUserRecordSelection.isRecordSelectedForImport(r.getIdentifier());
            RecordForImportComponent recordForImport = new RecordForImportComponent(r.getIdentifier(), isSelected,
                    currentUserRecordSelection);

            recordsForImport.add(recordForImport);
            add(recordForImport);
        }
    }
}