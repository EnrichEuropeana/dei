package pl.psnc.dei.ui.components.batches;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.ui.components.CommonComponentsFactory;

import java.util.Collections;
import java.util.function.Predicate;

public class BatchImportComponent extends VerticalLayout {

    private ProjectsRepository projectsRepository;

    private Select<Aggregator> aggregators;
    private Select<Project> projects;
    private Select<Dataset> datasets;

    private Aggregator aggregator;
    private Project project;
    private Dataset dataset;

    public BatchImportComponent(ProjectsRepository projectsRepository) {
        this.projectsRepository = projectsRepository;
        initLayout();
    }

    private void initLayout() {
        Div pageContainer = new Div();
        pageContainer.setId("batch-import-component");
        pageContainer.add(prepareAggregatorProjectDatasetSelector());
        pageContainer.add(prepareImportNameInputField());
        pageContainer.add(new Label("Records:"));
        add(pageContainer);
        add(prepareRecordsTextArea());
    }

    private HorizontalLayout prepareAggregatorProjectDatasetSelector() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(createAggregatorSelection());
        horizontalLayout.add(createProjectSelection());
        horizontalLayout.add(createDatasetsSelection());
        return horizontalLayout;
    }

    private HorizontalLayout prepareImportNameInputField() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addClassName("vertically-centered-row");
        horizontalLayout.add(new Label("Import name"));
        horizontalLayout.add(createImportNameInputField());
        return horizontalLayout;
    }

    private Component createAggregatorSelection() {
        Predicate<Aggregator> predicate = agg -> agg.equals(Aggregator.EUROPEANA);
        aggregators = CommonComponentsFactory.getAggregatorSelector(predicate);
        aggregators.setEnabled(false);
        aggregators.setValue(Aggregator.EUROPEANA);
        return aggregators;
    }

    private Component createProjectSelection() {
        projects = CommonComponentsFactory.getProjectSelector(projectsRepository);
        projects.addValueChangeListener(event ->
                //TODO: To implement
                System.out.println(event.toString())
        );
        return projects;
    }

    private Component createDatasetsSelection() {
        datasets = CommonComponentsFactory.getDatasetSelector(Collections.emptyList());
        datasets.addValueChangeListener(event ->
                //TODO: To implement
                System.out.println(event.toString())
        );
        return datasets;
    }

    private Component createImportNameInputField() {
        TextField input = new TextField();
        input.addClassName("flex-1");
        return input;
    }

    private TextArea prepareRecordsTextArea() {
        TextArea textArea = new TextArea();
        textArea.setMinHeight("150px");
        textArea.setWidthFull();
        textArea.addClassName("query-form");
        return textArea;
    }
}
