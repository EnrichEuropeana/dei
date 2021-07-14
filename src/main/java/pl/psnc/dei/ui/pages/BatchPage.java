package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.batches.BatchNavigationMenu;

@Route(value = "batch", layout = MainView.class)
public class BatchPage extends HorizontalLayout {

    private VerticalLayout displayingPlace;

    public BatchPage() {
        add(new BatchNavigationMenu(this));
        setWidthFull();
        setHeightFull();
		showBatchImportsView();
    }

    public void showBatchImportsView() {
    	//TODO: EN-153
		switchPage(new Paragraph("Imports - not implemented yet"));
    }

	public void showComplexImportsView() {
		//TODO: EN-154
		switchPage(new Paragraph("Complex imports - not implemented yet"));
	}

	private void switchPage(Component component) {
		if (displayingPlace != null) {
			remove(displayingPlace);
		}
		displayingPlace = new VerticalLayout();
		displayingPlace.add(component);
		add(displayingPlace);
	}
}


