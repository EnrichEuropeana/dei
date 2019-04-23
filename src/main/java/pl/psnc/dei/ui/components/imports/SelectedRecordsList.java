package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import pl.psnc.dei.model.Record;

import java.util.List;

/**
 * Component for storing and displaying selected records.
 * <p>
 * Created by pwozniak on 4/10/19
 */
public class SelectedRecordsList extends VerticalLayout {

    public SelectedRecordsList() {

    }

    public void update(List<Record> records) {
        this.removeAll();

        for (Record r : records) {
            Checkbox checkbox = new Checkbox();
            TextField t = new TextField();
            t.setValue(r.getIdentifier());
            t.setWidth("80%");
            this.add(checkbox, t);
        }
    }
}
