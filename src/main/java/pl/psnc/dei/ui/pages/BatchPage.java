package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.batches.BatchImportComponent;
import pl.psnc.dei.ui.components.batches.BatchNavigationMenu;

@Route(value = "batch", layout = MainView.class)
public class BatchPage extends HorizontalLayout {

	private ProjectsRepository projectsRepository;

    private VerticalLayout displayingPlace;

    public BatchPage(ProjectsRepository projectsRepository) {
    	this.projectsRepository = projectsRepository;
		setupComponents();
    }

    private void setupComponents() {
		add(new BatchNavigationMenu(this));
		setWidthFull();
		setHeightFull();
		showBatchImportsView();
	}

    public void showBatchImportsView() {
    	//TODO: EN-153
		switchPage(new BatchImportComponent(projectsRepository));
    }

	public void showComplexImportsView() {
		//TODO: EN-154
		switchPage(new VerticalLayout());
	}

	private void switchPage(VerticalLayout layout) {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = layout;
		add(displayingPlace);
	}
}


