package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.IntegerField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckboxWithIntegerInput extends Label {

    private Checkbox checkbox;
    private ClickableLabel label;
    private IntegerField integerField;
    private ClickableLabel suffix;

    public CheckboxWithIntegerInput() {
        super();
        checkbox = new Checkbox();
        label = new ClickableLabel();
        label.addClassName("margin-left-6px");
        label.addClickListener(this::onClickEvent);
        integerField = new IntegerField();
        integerField.addClassName("margin-left-6px");
        suffix = new ClickableLabel();
        suffix.addClassName("margin-left-6px");
        suffix.addClickListener(this::onClickEvent);
        add(checkbox, label, integerField, suffix);
        this.addClassName("vertically-centered-row");
    }

    public CheckboxWithIntegerInput(String label) {
        this();
        this.label.setText(label);
    }

    public CheckboxWithIntegerInput(String label, String suffix) {
        this(label);
        this.suffix.setText(suffix);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        checkbox.setEnabled(isEnabled);
        if (!isEnabled) {
            checkbox.setValue(false);
        }
        label.setEnabled(isEnabled);
        suffix.setEnabled(isEnabled);
        super.setEnabled(isEnabled);
    }

    private void onClickEvent(ComponentEvent<?> clickEvent) {
        checkbox.setValue(!checkbox.getValue());
    }
}
