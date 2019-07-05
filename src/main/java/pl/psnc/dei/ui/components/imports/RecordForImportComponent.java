package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.model.Record;

public class RecordForImportComponent extends HorizontalLayout {

	private Checkbox recordSelectedCheckBox = new Checkbox();
	private Label recordIdLabel = new Label();

	private String recordId;
	private CurrentUserRecordSelection currentUserRecordSelection;

	public RecordForImportComponent(String recordId, boolean isSelected, CurrentUserRecordSelection currentUserRecordSelection) {
		this.recordId = recordId;
		this.currentUserRecordSelection = currentUserRecordSelection;

		if (isSelected) {
			addRecordIdForImport();
		}

		recordIdLabel.setText(recordId);
		recordIdLabel.setWidth("80%");
		recordSelectedCheckBox.setValue(isSelected);
		recordSelectedCheckBox.addValueChangeListener(e -> {
			if (e.getValue()) {
				addRecordIdForImport();
			} else {
				removeRecordIdForImport();
			}
		});

		add(recordSelectedCheckBox, recordIdLabel);
	}

	private void addRecordIdForImport() {
		currentUserRecordSelection.addSelectedRecordIdForImport(new Record(recordId));
	}

	private void removeRecordIdForImport() {
		currentUserRecordSelection.removeSelectedRecordIdForImport(recordId);
	}

	public String getRecordId() {
		return recordId;
	}
}
