package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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
import pl.psnc.dei.service.*;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.ConfirmationDialog;
import pl.psnc.dei.ui.components.FacetComponent;
import pl.psnc.dei.ui.components.SearchResultsComponent;

import java.util.*;
import java.util.stream.Collectors;

import static pl.psnc.dei.ui.components.FacetComponent.DEFAULT_FACETS;


@Route(value = "search", layout = MainView.class)
@Secured(Role.OPERATOR)
public class SearchPage extends HorizontalLayout implements HasUrlParameter<String>, BeforeLeaveObserver, AfterNavigationObserver {

    public static final String QUERY_PARAM_NAME = "query";
    public static final String QF_PARAM_NAME = "qf";
    public static final String CURSOR_PARAM_NAME = "cursor";
    public static final String ONLY_IIIF_PARAM_NAME = "only_iiif";

    private TextField search;

    private Checkbox searchOnlyIiif;

    private FacetComponent facets;

    private SearchResultsComponent resultsComponent;

    private TranscriptionPlatformService transcriptionPlatformService;

    private EuropeanaRestService europeanaRestService;

    private CurrentUserRecordSelection currentUserRecordSelection;

    private RecordsProjectsAssignmentService recordsProjectsAssignmentService;

    private UIPollingManager uiPollingManager;

	private RecordTransferValidationCache recordTransferValidationCache;

    // label used when no results were found
    private Label noResults;

    private Button invertSelectionButton;

    private Button selectAllButton;

    private Button addElementsButton;

    private String originalLocation;

    private static boolean onlyIiif = true;

    public SearchPage(
            SearchController searchController,
            TranscriptionPlatformService transcriptionPlatformService,
            EuropeanaRestService europeanaRestService,
            CurrentUserRecordSelection currentUserRecordSelection,
            RecordsProjectsAssignmentService recordsProjectsAssignmentService,
            UIPollingManager uiPollingManager,
			RecordTransferValidationCache recordTransferValidationCache) {
        this.transcriptionPlatformService = transcriptionPlatformService;
        this.europeanaRestService = europeanaRestService;
        this.currentUserRecordSelection = currentUserRecordSelection;
        this.recordsProjectsAssignmentService = recordsProjectsAssignmentService;
        this.uiPollingManager = uiPollingManager;
        this.recordTransferValidationCache = recordTransferValidationCache;
        setDefaultVerticalComponentAlignment(Alignment.START);
        setAlignSelf(Alignment.STRETCH, this);

        Component searchResultsList = createSearchResultsList(searchController);
        createFacetComponent();
        add(facets, searchResultsList);
        expand(searchResultsList);
    }

    /**
     * Prepare QueryParameters
     *
     * @param query  query string
     * @param qf     query filter
     * @param cursor cursor
     * @param otherParams other request parameters e.g. media, requsability
     * @return QueryParameters used by the search page
     */
    public static QueryParameters prepareQueryParameters(String query, String qf, String cursor, Map<String, String> otherParams) {
        Map<String, List<String>> parameters = new HashMap<>();
        addParameter(QUERY_PARAM_NAME, query, parameters);
        addParameter(QF_PARAM_NAME, qf, parameters);
        addParameter(CURSOR_PARAM_NAME, cursor, parameters);
        addParameter(ONLY_IIIF_PARAM_NAME, String.valueOf(onlyIiif), parameters);
        otherParams.forEach((s, s2) -> addParameter(s.toLowerCase(), s2, parameters));
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
        createSearchOnlyIiifBox();
        searchResultsList.add(searchOnlyIiif);
        createNoResultsLabel();
        searchResultsList.add(noResults);
        resultsComponent = new SearchResultsComponent(searchController, currentUserRecordSelection,
                europeanaRestService, uiPollingManager, recordTransferValidationCache);
        searchResultsList.add(
                createProjectSelectionBox(),
                createSelectionProperties(),
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

    private void createSearchOnlyIiifBox() {
        searchOnlyIiif = new Checkbox();
        searchOnlyIiif.setLabel("Search objects only available via IIIF");
        searchOnlyIiif.addValueChangeListener(e -> setOnlyIiif(e.getValue()));
        searchOnlyIiif.setValue(true);
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

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(projects, datasets);
        return layout;
    }

    private Component createSelectionProperties() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        selectAllButton = new Button("Select all");
        selectAllButton.setVisible(false);
        selectAllButton.addClickListener(e -> resultsComponent.selectAll());
        invertSelectionButton = new Button("Invert selection");
        invertSelectionButton.addClickListener(e -> resultsComponent.inverseSelection());
        invertSelectionButton.setVisible(false);

        addElementsButton = new Button();
        addElementsButton.setText("Add");
        addElementsButton.setVisible(false);
        addElementsButton.addClickListener(
                e -> {
                    recordsProjectsAssignmentService.saveSelectedRecords();
					Notification.show(currentUserRecordSelection.getSelectedRecordIds().size() + " record(s) added!",
							3000, Notification.Position.TOP_CENTER);
					UI ui = UI.getCurrent();
					uiPollingManager.registerPollRequest(ui, addElementsButton, 100);
					ui.access(() -> {
                    	resultsComponent.deselectAll();
                    	uiPollingManager.unregisterPollRequest(ui, addElementsButton);
                    });
					currentUserRecordSelection.clearSelectedRecords();
                });

        horizontalLayout.add(selectAllButton, invertSelectionButton, addElementsButton);
        return horizontalLayout;
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
        search.setAutofocus(true);
        search.addKeyUpListener(Key.ENTER,
                keyUpEvent -> search.getUI().ifPresent(ui -> ui.navigate("search",
                        prepareQueryParameters(search.getValue(), null, SearchResults.FIRST_CURSOR, DEFAULT_FACETS))));

        Button searchButton = new Button();
        searchButton.setIcon(new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(
                e -> e.getSource().getUI().ifPresent(ui -> ui.navigate("search",
                        prepareQueryParameters(search.getValue(), null, SearchResults.FIRST_CURSOR, DEFAULT_FACETS))));
        queryForm.add(search, searchButton);
        queryForm.expand(search);
        queryForm.setDefaultVerticalComponentAlignment(Alignment.START);
        queryForm.expand();
        return queryForm;
    }

    private void search(String query, String qf, String cursor, Map<String, List<String>> requestParams) {
        if (!currentUserRecordSelection.getSelectedRecordIds().isEmpty()) {
            ConfirmationDialog dialog = new ConfirmationDialog("Not added records",
                    "There are " + currentUserRecordSelection.getSelectedRecordIds().size()
                            + " selected but not added record(s). Record selection will be lost with next search query execution.",
                    e -> executeSearch(query, qf, cursor, requestParams));
            dialog.addContent("Are you sure you want to continue?");
            dialog.open();
        } else {
            executeSearch(query, qf, cursor, requestParams);
        }
    }

    /**
     * Execute search in the SearchResultsComponent and add facets
     *
     * @param query  query string
     * @param qf     query filter
     * @param cursor cursor
     * @param requestParams other request parameters e.g. media, reusability
     */
    private void executeSearch(String query, String qf, String cursor, Map<String, List<String>> requestParams) {
		currentUserRecordSelection.clearSelectedRecords();
        if (query == null || query.isEmpty()) {
            resultsComponent.clear();
            facets.addFacets(null);
            showFacets(false);
            search.clear();
        } else {
            if (search.isEmpty()) {
                search.setValue(query);
            }
            SearchResults results = resultsComponent.executeSearch(query, qf, cursor, onlyIiif, requestParams);
            if (results != null) {
                facets.addFacets(results.getFacets());
                facets.updateState(qf, requestParams);
                showFacets(true);
                noResults.setVisible(results.getTotalResults() == 0);
                invertSelectionButton.setVisible(results.getTotalResults() > 0);
                selectAllButton.setVisible(results.getTotalResults() > 0);
                addElementsButton.setVisible(results.getTotalResults() > 0);
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
            String query = getParameterValue(parametersMap.get(QUERY_PARAM_NAME), true);
            String qf = getParameterValue(parametersMap.get(QF_PARAM_NAME), false);
            String cursor = getParameterValue(parametersMap.get(CURSOR_PARAM_NAME), true);
            String onlyIiifParam = getParameterValue(parametersMap.get(ONLY_IIIF_PARAM_NAME), true);
            setOnlyIiif(onlyIiifParam == null || Boolean.parseBoolean(onlyIiifParam));
            searchOnlyIiif.setValue(onlyIiif);
            Map<String, List<String>> requestParams = new HashMap<>();
            parametersMap.entrySet().stream()
                    .filter(e -> {
                                String key = e.getKey();
                                return !(key.equalsIgnoreCase(QUERY_PARAM_NAME) || key.equalsIgnoreCase(QF_PARAM_NAME)
                                        || key.equalsIgnoreCase(CURSOR_PARAM_NAME) || key.equalsIgnoreCase(ONLY_IIIF_PARAM_NAME));
                            })
                    .forEach(e -> requestParams.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue()));
            search(query, qf, cursor, requestParams);
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

    public static void setOnlyIiif(boolean onlyIiif) {
        SearchPage.onlyIiif = onlyIiif;
    }

    @Override
	protected void onDetach(DetachEvent detachEvent) {
		currentUserRecordSelection.clearSelectedRecords();
		uiPollingManager.unregisterAllPollRequests(UI.getCurrent());
		recordTransferValidationCache.clear();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		// store the current location so that we can restore that in beforeLeave if needed
		originalLocation = afterNavigationEvent.getLocation().getPathWithQueryParameters();
	}

	@Override
	public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
		if (!currentUserRecordSelection.getSelectedRecordIds().isEmpty() && beforeLeaveEvent.getNavigationTarget() != getClass()) {
			BeforeLeaveEvent.ContinueNavigationAction action = beforeLeaveEvent.postpone();

			// replace the top most history state in the browser with this view's location
			// https://github.com/vaadin/flow/issues/3619
			UI.getCurrent().getPage().executeJavaScript("history.replaceState({},'','" + originalLocation + "');");

			ConfirmationDialog dialog = new ConfirmationDialog("Not added records",
					"There are " + currentUserRecordSelection.getSelectedRecordIds().size()
							+ " selected but not added record(s). Record selection will be lost if you leave this page.",
					e -> {
						action.proceed();
						// update the address bar to reflect location of the view where the user was trying to navigate to
						String destination = beforeLeaveEvent.getLocation().getPathWithQueryParameters();
						UI.getCurrent().getPage().executeJavaScript("history.replaceState({},'','" + destination + "');");
					});
			dialog.addContent("Are you sure you want to continue?");
			dialog.open();
		}
	}
}
