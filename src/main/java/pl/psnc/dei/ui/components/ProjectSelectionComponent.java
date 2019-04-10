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

    private Select projects = new Select<>();
    private Select datasets = new Select<>();
    private TranscriptionPlatformService transcriptionPlatformService;
    private HasValue.ValueChangeListener projectChangeListener;
    private HasValue.ValueChangeListener datasetChangeListener;

    public ProjectSelectionComponent(
            TranscriptionPlatformService transcriptionPlatformService,
            HasValue.ValueChangeListener projectChangeListener,
            HasValue.ValueChangeListener datasetChangeListener) {
        //
        this.transcriptionPlatformService = transcriptionPlatformService;
        this.projectChangeListener = projectChangeListener;
        this.datasetChangeListener = datasetChangeListener;
        //
        add(projectsList(), datasetsList());
    }

    private Select<Project> projectsList() {
        projects.setLabel("Available projects");
        projects.setItems(transcriptionPlatformService.getProjects());
        projects.setEmptySelectionAllowed(false);

        projects.addValueChangeListener(event -> {
            Project project = (Project) event.getValue();
            datasets.setItems(project.getDatasets());
        });
        projects.addValueChangeListener(projectChangeListener);
        return projects;
    }

    private Select<Dataset> datasetsList() {
        datasets.setLabel("Available datasets");
        datasets.setEmptySelectionAllowed(true);
        datasets.addValueChangeListener(event -> {
            Dataset selectedDataset = (Dataset) event.getValue();
        });
        add(projects, datasets);
        return datasets;
    }

}