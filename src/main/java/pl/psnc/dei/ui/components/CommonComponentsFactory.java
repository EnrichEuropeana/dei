package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.ListDataProvider;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommonComponentsFactory {

    public static Select<Aggregator> getAggregatorSelector() {
        Predicate<Aggregator> predicate = (agg -> !agg.equals(Aggregator.UNKNOWN));
        return getAggregatorSelector(predicate);
    }

    public static Select<Aggregator> getAggregatorSelector(Predicate<Aggregator> aggregatorFilter) {
        Select<Aggregator> aggregators = new Select<>();
        ListDataProvider<Aggregator> aggregatorsDataProvider = new ListDataProvider<>(Arrays.stream(Aggregator.values()).filter(aggregatorFilter).collect(Collectors.toList()));
        aggregators.setDataProvider(aggregatorsDataProvider);
        aggregators.setLabel("Select aggregator");
        return aggregators;
    }

    public static Select<Project> getProjectSelector(ProjectsRepository projectsRepository) {
        Select<Project> projects = new Select<>();
        ListDataProvider<Project> listDataProvider = new ListDataProvider<>(projectsRepository.findAll());
        projects.setDataProvider(listDataProvider);
        projects.setLabel("Select project");
        return projects;
    }

    public static Select<Dataset> getDatasetSelector(List<Dataset> datasetList) {
        Select<Dataset> datasets = new Select<>();
        datasets.setItems(datasetList);
        datasets.setLabel("Available datasets");
        return datasets;
    }
}
