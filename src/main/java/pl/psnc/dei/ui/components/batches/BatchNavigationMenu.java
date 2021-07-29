package pl.psnc.dei.ui.components.batches;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.ui.pages.BatchPage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@StyleSheet("./styles/styles.css")
public class BatchNavigationMenu extends VerticalLayout {

    private BatchPage batchPage;
    private List<Button> elements = new ArrayList<>();

    public BatchNavigationMenu(BatchPage batchPage) {
        this.batchPage = batchPage;
        addClassName("facet-component");
        setSizeFull();
        add(createElements());
        underlineChoiceOnTheBeginning();
    }

    private Component createElements() {
        VerticalLayout layout = new VerticalLayout();
        Label label = new Label("Batch options");
        label.addClassName("metadata-label");
        layout.add(label);
        layout.add(createMenuItem("Upload records", e -> batchPage.showBatchImportsView()));
        layout.add(createMenuItem("Complex upload", e -> batchPage.showComplexImportsView()));
        return layout;
    }

    private Component createMenuItem(String buttonName, Consumer<Button> function) {
        Button button = new Button(buttonName);
        button.setClassName("button-as-text");
        button.addClickListener(e -> {
            underlineElement(button);
            function.accept(button);
        });
        button.addClassName("underlined-item");
        elements.add(button);
        return button;
    }

    private void underlineElement(Button element) {
        removeUnderline();
        element.addClassName("underlined-item");
        element.focus();
    }

    private void removeUnderline() {
        elements.forEach(button -> button.removeClassName("underlined-item"));
    }

    private void underlineChoiceOnTheBeginning() {
        underlineElement(elements.get(0));
    }
}