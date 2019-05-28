package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.service.ImportsHistoryService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.imports.ImportsListGenerator;

import java.util.List;

@Route(value = "history", layout = MainView.class)
public class ImportsHistory extends VerticalLayout {

	public ImportsHistory(ImportsHistoryService importsHistoryService, ImportPage importPage) {
		add(prepareImportsGrid(importsHistoryService.getAllImports(), importPage));
	}

	private Grid prepareImportsGrid(List<Import> imports, ImportPage importPage) {
		return new ImportsListGenerator(imports, importPage).generate();
	}
}
