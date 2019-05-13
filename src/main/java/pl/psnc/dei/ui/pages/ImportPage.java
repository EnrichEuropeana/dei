package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.service.ImportsHistoryService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.imports.ImportNavigationMenu;
import pl.psnc.dei.ui.components.imports.ImportsListComponent;

/**
 * Page for import generation.
 * <p>
 * Created by pwozniak on 4/8/19
 */
@Route(value = "import", layout = MainView.class)
public class ImportPage extends HorizontalLayout {

	private VerticalLayout displayingPlace;
	private ImportsRepository importsRepository;
	private ImportsHistoryService importsHistoryService;

	public ImportPage(ImportsRepository importsRepository
			, ImportsHistoryService importsHistoryService) {
		this.importsHistoryService = importsHistoryService;
		add(new ImportNavigationMenu(this));
		this.importsRepository = importsRepository;
		setWidthFull();
		setHeightFull();
	}

	public void createHistoryImports() {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new ImportsHistory(importsHistoryService);
		add(displayingPlace);
	}

	public void createListImports() {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new ImportsListComponent(importsRepository, this);
		add(displayingPlace);
	}
}


