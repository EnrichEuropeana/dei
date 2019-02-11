package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

public class FacetBox extends VerticalLayout {
    private Text name;

    private Icon iconUp;

    private Icon iconDown;

    private List<Checkbox> values;

    public FacetBox(String label, List<String> valueLabels) {
        getStyle().set("margin-top", "0px");
        getStyle().set("margin-left", "10px");
        setPadding(false);
        this.name = new Text(label);
        this.values = new ArrayList<>();
        valueLabels.forEach(s -> {
            Checkbox checkbox = new Checkbox(s);
            checkbox.getStyle().set("margin", "0px");
            checkbox.setVisible(false);
            this.values.add(checkbox);
        });

        iconUp = new Icon(VaadinIcon.ANGLE_UP);
        iconUp.setVisible(false);
        iconUp.addClickListener(iconClickEvent -> {
                iconUp.setVisible(false);
                iconDown.setVisible(true);
                values.forEach(checkbox -> checkbox.setVisible(false));
            });
        iconUp.getStyle().set("margin-left", "auto");

        iconDown = new Icon(VaadinIcon.ANGLE_DOWN);
        iconDown.setVisible(true);
        iconDown.addClickListener(iconClickEvent -> {
            iconUp.setVisible(true);
            iconDown.setVisible(false);
            values.forEach(checkbox -> checkbox.setVisible(true));
        });
        iconDown.getStyle().set("margin-left", "auto");

        HorizontalLayout nameLine = new HorizontalLayout();
        nameLine.setDefaultVerticalComponentAlignment(Alignment.STRETCH);
        nameLine.add(name, iconUp, iconDown);
        nameLine.setAlignSelf(Alignment.END, iconUp);
        nameLine.setAlignSelf(Alignment.END, iconDown);

        add(nameLine);
        add(values.toArray(new Checkbox[0]));
    }
}
