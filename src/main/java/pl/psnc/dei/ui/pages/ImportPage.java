package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.service.ImportsHistoryService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.imports.CandidatesListsComponent;
import pl.psnc.dei.ui.components.imports.CreateImportComponent;
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
	private ImportPackageService importPackageService;
	private ImportsHistoryService importsHistoryService;
	private RecordsRepository recordsRepository;
	private ProjectsRepository projectsRepository;

	public ImportPage(ImportsRepository importsRepository
			, ImportPackageService importPackageService, ImportsHistoryService importsHistoryService, RecordsRepository recordsRepository, ProjectsRepository projectsRepository) {
		this.importPackageService = importPackageService;
		this.importsHistoryService = importsHistoryService;
		this.recordsRepository = recordsRepository;
		this.projectsRepository = projectsRepository;
		add(new ImportNavigationMenu(this));
		this.importsRepository = importsRepository;
		setWidthFull();
		setHeightFull();
		showCreateListImportView();
	}

	public void showHistoryImportsView() {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new ImportsHistory(importsHistoryService);
		add(displayingPlace);
	}

	public void showCreateListImportView() {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new ImportsListComponent(importsRepository, this);
		add(displayingPlace);
	}

	public void showCreateImportView() {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new CreateImportComponent(importPackageService, recordsRepository, projectsRepository, this);
		add(displayingPlace);
	}

	public void showEditImportView(Import anImport) {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new CreateImportComponent(importPackageService, anImport, recordsRepository, projectsRepository, this);
		add(displayingPlace);
	}

	public void showSendImportView(Import anImport) {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new CreateImportComponent(importPackageService, anImport, recordsRepository, projectsRepository, this);
		add(displayingPlace);
	}

	public void showCandidatesView() {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new CandidatesListsComponent(projectsRepository, recordsRepository);
		add(displayingPlace);
	}
}


