package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.ui.pages.ImportPage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@StyleSheet("frontend://styles/styles.css")
public class ImportNavigationMenu extends VerticalLayout {

	private ImportPage importPage;
	List<Button> elements = new ArrayList<>();

	public ImportNavigationMenu(ImportPage importPage) {
		this.importPage = importPage;
		addClassName("facet-component");
		setSizeFull();
		add(createElements());
		underlineChoiceOnTheBeginning();
	}

	private Component createElements() {
		VerticalLayout layout = new VerticalLayout();
		Label label = new Label("Imports options");
		label.addClassName("metadata-label");
		layout.add(label);
		layout.add(createElement(new Button("Candidate records"), e -> {
			underlineElement(e);
			importPage.showCandidatesView();
		}));
		layout.add(createElement(new Button("Create import"), e -> {
			underlineElement(e);
			importPage.showCreateImportView();
		}));
		layout.add(createElement(new Button("List imports"), e -> {
			underlineElement(e);
			importPage.showCreateListImportView();
		}));
		layout.add(createElement(new Button("Imports history"), e -> {
			underlineElement(e);
			importPage.showHistoryImportsView();
		}));
		return layout;
	}

	private Component createElement(Button button, Consumer<Button> function){
		button.setClassName("button-as-text");
		button.addClickListener(e -> {function.accept(button);});
		button.addClassName("underlined-item");
		elements.add(button);
		return button;
	}

	private void underlineElement(Button element) {
		removeUnderline();
		element.addClassName("underlined-item");
		element.focus();
	}

	private void removeUnderline(){
		elements.forEach(button -> {
			button.removeClassName("underlined-item");
		});
	}

	private void underlineChoiceOnTheBeginning() {
		underlineElement(elements.get(0));
	}
}