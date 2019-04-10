package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.service.TranscriptionPlatformService;


/**
 * Component that groups other components related with project and dataset selection.
 * <p>
 * Created by pwozniak on 4/10/19
 */
public class ProjectSelectionComponent extends HorizontalLayout {

    Select projects = new Select<>();
    Select datasets = new Select<>();

    public ProjectSelectionComponent(
            TranscriptionPlatformService transcriptionPlatformService,
            HasValue.ValueChangeListener projectChangeListener,
            HasValue.ValueChangeListener datasetChangeListener) {
        //
        projects.setItems(transcriptionPlatformService.getProjects());
        projects.setLabel("Available projects");
        projects.setEmptySelectionAllowed(false);

        projects.addValueChangeListener(event -> {
            Project project = (Project) event.getValue();
            datasets.setItems(project.getDatasets());
        });
        projects.addValueChangeListener(projectChangeListener);
        //
        datasets.setLabel("Available datasets");
        datasets.setEmptySelectionAllowed(true);
        datasets.addValueChangeListener(event -> {
            Dataset selectedDataset = (Dataset) event.getValue();
        });
        add(projects, datasets);
    }

}