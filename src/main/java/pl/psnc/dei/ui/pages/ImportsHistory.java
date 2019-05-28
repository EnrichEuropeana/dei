package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.service.ImportsHistoryService;
import pl.psnc.dei.ui.components.imports.ImportsListGenerator;

import java.util.List;

class ImportsHistory extends VerticalLayout {

	ImportsHistory(ImportsHistoryService importsHistoryService, ImportPage importPage) {
		add(prepareImportsGrid(importsHistoryService.getAllImports(), importPage));
	}

	private Grid prepareImportsGrid(List<Import> imports, ImportPage importPage) {
		return new ImportsListGenerator(imports, importPage).generate();
	}
}
