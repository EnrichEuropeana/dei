package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.service.BatchService;
import pl.psnc.dei.service.ImportPackageService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.batches.BatchImportComponent;
import pl.psnc.dei.ui.components.batches.BatchNavigationMenu;
import pl.psnc.dei.ui.components.batches.ComplexBatchImportComponent;

@Route(value = "batch", layout = MainView.class)
public class BatchPage extends HorizontalLayout {

    private final ProjectsRepository projectsRepository;
    private final BatchService batchService;
    private final ImportPackageService importPackageService;

    private VerticalLayout displayingPlace;

    public BatchPage(ProjectsRepository projectsRepository, BatchService batchService,
                     ImportPackageService importPackageService) {
        this.projectsRepository = projectsRepository;
        this.batchService = batchService;
        this.importPackageService = importPackageService;
        setupComponents();
    }

    private void setupComponents() {
        add(new BatchNavigationMenu(this));
        setWidthFull();
        setHeightFull();
        showBatchImportsView();
    }

    public void showBatchImportsView() {
        switchPage(new BatchImportComponent(projectsRepository, batchService, importPackageService));
    }

    public void showComplexImportsView() {
        switchPage(new ComplexBatchImportComponent(projectsRepository, batchService));
    }

    private void switchPage(VerticalLayout layout) {
        if (displayingPlace != null) {
            remove(displayingPlace);
        }
        displayingPlace = layout;
        add(displayingPlace);
    }
}


