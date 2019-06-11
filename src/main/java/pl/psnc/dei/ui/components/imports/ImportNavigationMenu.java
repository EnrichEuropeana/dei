package pl.psnc.dei.ui.components.imports;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.ui.pages.ImportPage;

import java.util.function.Consumer;

@StyleSheet("frontend://styles/styles.css")
public class ImportNavigationMenu extends VerticalLayout {

	private ImportPage importPage;

	public ImportNavigationMenu(ImportPage importPage) {
		this.importPage = importPage;
		addClassName("facet-component");
		setSizeFull();
		add(createElements());
	}

	private Component createElements() {
		VerticalLayout layout = new VerticalLayout();
		Label label = new Label("Imports options");
		label.addClassName("metadata-label");
		layout.add(label);
		layout.add(createElement(new Button("Create import"), e -> {
			importPage.showCreateImportView();
		}));
		layout.add(createElement(new Button("List imports"), e -> {
			importPage.showCreateListImportView();
		}));
		layout.add(createElement(new Button("Imports history"), e -> {
			importPage.showHistoryImportsView();
		}));
		layout.add(createElement(new Button("Current candidates"), e -> {
			importPage.showCandidatesView();
		}));
		return layout;
	}

	private Component createElement(Button button, Consumer function){
		button.setClassName("button-as-text");
		button.addClickListener(function::accept);
		return button;
	}

}