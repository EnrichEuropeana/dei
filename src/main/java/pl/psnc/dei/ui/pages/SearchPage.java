package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.security.access.annotation.Secured;
import pl.psnc.dei.config.Role;
import pl.psnc.dei.controllers.SearchController;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.schema.search.SearchResults;
import pl.psnc.dei.service.RecordsProjectsAssignmentService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.FacetComponent;
import pl.psnc.dei.ui.components.SearchResultsComponent;

import java.util.*;
import java.util.stream.Collectors;


@Route(value = "search", layout = MainView.class)
@Secured(Role.OPERATOR)
public class SearchPage extends HorizontalLayout implements HasUrlParameter<String> {
    private TextField search;

    private FacetComponent facets;

    private SearchResultsComponent resultsComponent;

    private TranscriptionPlatformService transcriptionPlatformService;

    private CurrentUserRecordSelection currentUserRecordSelection;

    private RecordsProjectsAssignmentService recordsProjectsAssignmentService;

    // label used when no results were found
    private Label noResults;

    public SearchPage(
            SearchController searchController,
            TranscriptionPlatformService transcriptionPlatformService,
            CurrentUserRecordSelection currentUserRecordSelection,
            RecordsProjectsAssignmentService recordsProjectsAssignmentService) {
        this.transcriptionPlatformService = transcriptionPlatformService;
        this.currentUserRecordSelection = currentUserRecordSelection;
        this.recordsProjectsAssignmentService = recordsProjectsAssignmentService;
        setDefaultVerticalComponentAlignment(Alignment.START);
        setAlignSelf(Alignment.STRETCH, this);

        Component searchResultsList = createSearchResultsList(searchController);
        createFacetComponent();
        add(facets, searchResultsList);
        expand(searchResultsList);
    }

    /**
     * Creates facets component. By default it is hidden.
     */
    private void createFacetComponent() {
        facets = new FacetComponent(resultsComponent);
        facets.setPadding(false);
        showFacets(false);
    }

    /**
     * Show / hide facets component using visibility css property
     *
     * @param show when true
     */
    private void showFacets(boolean show) {
        if (show) {
            facets.removeClassName("facet-hidden");
        } else {
            facets.addClassName("facet-hidden");
        }
    }

    /**
     * Creates search results list component which consists of the query form and the search results component
     *
     * @param searchController search controller used to execute searches
     * @return created component
     */
    private Component createSearchResultsList(SearchController searchController) {
        VerticalLayout searchResultsList = new VerticalLayout();
        searchResultsList.add(createQueryForm());
        createNoResultsLabel();
        searchResultsList.add(noResults);
        resultsComponent = new SearchResultsComponent(searchController, currentUserRecordSelection);
        searchResultsList.add(
                createProjectSelectionBox(),
                resultsComponent);
        return searchResultsList;
    }

    /**
     * Creates label to be shown when no results were found
     */
    private void createNoResultsLabel() {
        noResults = new Label("No results found! Try refining your query.");
        noResults.addClassName("no-results-label");
        noResults.setVisible(false);
        add(noResults);
    }

    private Component createProjectSelectionBox() {
        //
        Project currentProject = transcriptionPlatformService.getProjects().iterator().next();
        //
        Select<Project> projects = new Select<>();
        Select<Dataset> datasets = new Select<>();
        //
        projects.setItems(transcriptionPlatformService.getProjects());
        projects.setLabel("Available projects");
        projects.setEmptySelectionAllowed(false);
        projects.addValueChangeListener(event -> {
            Project project = (Project) event.getValue();
            datasets.setItems(project.getDatasets());
            currentUserRecordSelection.setSelectedProject(project);
        });
        projects.setValue(currentProject);
        //
        datasets.setItems(currentProject.getDatasets());
        datasets.setLabel("Available datasets");
        datasets.setEmptySelectionAllowed(true);
        datasets.addValueChangeListener(event -> {
            Dataset selectedDataset = (Dataset) event.getValue();
            currentUserRecordSelection.setSelectedDataSet(selectedDataset);
        });
        //
        //
        Button addElements = new Button();
        addElements.setText("Add");

        addElements.addClickListener(
                e -> {
                    recordsProjectsAssignmentService.saveSelectedRecords();
                    currentUserRecordSelection.clearSelectedRecords();
                    UI.getCurrent().getPage().reload();
                });
        //
        HorizontalLayout layout = new HorizontalLayout();
        layout.add(projects, datasets, addElements);
        layout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        return layout;
    }

    /**
     * Create query form with search field and button
     *
     * @return created component
     */
    private Component createQueryForm() {
        HorizontalLayout queryForm = new HorizontalLayout();
        queryForm.addClassName("query-form");
        queryForm.setPadding(false);
        queryForm.setSizeFull();

        search = new TextField();
        search.addClassName("search-field");
        search.setPlaceholder("Search in Europeana");
        search.addKeyUpListener(Key.ENTER,
                keyUpEvent -> {
                    currentUserRecordSelection.clearSelectedRecords();
                    search.getUI().ifPresent(ui -> ui.navigate("search", prepareQueryParameters(search.getValue(), null, SearchResults.FIRST_CURSOR)));
                });

        Button searchButton = new Button();
        searchButton.setIcon(new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(
                e -> {
                    currentUserRecordSelection.clearSelectedRecords();
                    e.getSource().getUI().ifPresent(ui -> ui.navigate("search", prepareQueryParameters(search.getValue(), null, SearchResults.FIRST_CURSOR)));
                });
        queryForm.add(search, searchButton);
        queryForm.expand(search);
        queryForm.setDefaultVerticalComponentAlignment(Alignment.START);
        queryForm.expand();
        return queryForm;
    }

    /**
     * Prepare QueryParameters
     *
     * @param query  query string
     * @param qf     query filter
     * @param cursor cursor
     * @return QueryParameters used by the search page
     */
    public static QueryParameters prepareQueryParameters(String query, String qf, String cursor) {
        Map<String, List<String>> parameters = new HashMap<>();
        addParameter("query", query, parameters);
        addParameter("qf", qf, parameters);
        addParameter("cursor", cursor, parameters);
        return new QueryParameters(parameters);
    }

    /**
     * Adds a parameter as a list of values. Values are retrieved from <code>value</code> by splitting it with '&' delimiter
     *
     * @param name       parameter name
     * @param value      value of the parameter (may be concatenation of many values val1&val2&...&valn
     * @param parameters map of parameters
     */
    private static void addParameter(String name, String value, Map<String, List<String>> parameters) {
        if (value == null) {
            return;
        }
        String[] split = value.split("&");
        List<String> values = new ArrayList<>();
        Collections.addAll(values, split);
        parameters.put(name, values);
    }

    /**
     * Execute search in the SearchResultsComponent and add facets
     *
     * @param query  query string
     * @param qf     query filter
     * @param cursor cursor
     */
    private void executeSearch(String query, String qf, String cursor) {
        if (query == null || query.isEmpty()) {
            resultsComponent.clear();
            facets.addFacets(null);
            showFacets(false);
            search.clear();
        } else {
            if (search.isEmpty()) {
                search.setValue(query);
            }
            SearchResults results = resultsComponent.executeSearch(query, qf, cursor);
            if (results != null) {
                facets.addFacets(results.getFacets());
                facets.updateState(qf);
                showFacets(true);
                noResults.setVisible(results.getTotalResults() == 0);
            }
        }
    }

    /**
     * Executed before search page is displayed. Handles the query parameters from URL.
     *
     * @param event     event used to the current URL
     * @param parameter not used here
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        if (queryParameters != null) {
            Map<String, List<String>> parametersMap = queryParameters.getParameters();
            String query = getParameterValue(parametersMap.get("query"), true);
            String qf = getParameterValue(parametersMap.get("qf"), false);
            String cursor = getParameterValue(parametersMap.get("cursor"), true);
            executeSearch(query, qf, cursor);
        }
    }

    /**
     * Gets a single value for a parameter. If <code>oneValue</code> is true only the first from the list is returned
     * otherwise values are concatenated with "AND"
     *
     * @param values   list of values
     * @param oneValue one value indicator
     * @return value
     */
    private String getParameterValue(List<String> values, boolean oneValue) {
        if (values != null && !values.isEmpty()) {
            if (oneValue || values.size() == 1) {
                return values.get(0);
            } else {
                // surround with brackets and join with AND
                return values.stream().map(s -> "(" + s + ")").collect(Collectors.joining(" AND "));
            }
        }
        return null;
    }
}
