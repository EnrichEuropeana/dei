package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.ui.MainView;

@Route(value = "accessdenied", layout = MainView.class)
public class AccessDeniedPage extends VerticalLayout {

    AccessDeniedPage() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(new Label("Access denied!"));
        add(formLayout);
    }
}
