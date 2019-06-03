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
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.schema.search.SearchResults;
import pl.psnc.dei.service.*;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.ConfirmationDialog;
import pl.psnc.dei.ui.components.SearchResultsComponent;
import pl.psnc.dei.ui.components.facets.FacetComponent;
import pl.psnc.dei.ui.components.facets.FacetComponentFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Route(value = "search", layout = MainView.class)
@Secured(Role.OPERATOR)
public class SearchPage extends HorizontalLayout implements HasUrlParameter<String>, BeforeLeaveObserver, AfterNavigationObserver {

    private static final String AGGREGATOR_PARAM_NAME = "aggregator";
    private static final String QUERY_PARAM_NAME = "query";
    public static final String ONLY_IIIF_PARAM_NAME = "only_iiif";

    private Select<Aggregator> aggregator;

    private TextField search;

    private Checkbox searchOnlyIiif;

    private FacetComponent facets;

    private SearchResultsComponent resultsComponent;

    private SearchService searchService;

    private TranscriptionPlatformService transcriptionPlatformService;

    private CurrentUserRecordSelection currentUserRecordSelection;

    private RecordsProjectsAssignmentService recordsProjectsAssignmentService;

	private SearchResponseFillerService searchResponseFillerService;

    // label used when no results were found
    private Label noResults;

    private Button invertSelectionButton;

    private Button selectAllButton;

    private Button addElementsButton;

    private String originalLocation;

    private static boolean onlyIiif = true;

    private String query;

    private Map<String, String> requestParams;

    public SearchPage(SearchService searchService,
                      TranscriptionPlatformService transcriptionPlatformService,
                      CurrentUserRecordSelection currentUserRecordSelection,
                      RecordsProjectsAssignmentService recordsProjectsAssignmentService,
                      SearchResponseFillerService searchResponseFillerService) {
        this.searchService = searchService;
        this.transcriptionPlatformService = transcriptionPlatformService;
        this.currentUserRecordSelection = currentUserRecordSelection;
        this.recordsProjectsAssignmentService = recordsProjectsAssignmentService;
        this.searchResponseFillerService = searchResponseFillerService;

        setDefaultVerticalComponentAlignment(Alignment.START);
        setAlignSelf(Alignment.STRETCH, this);

        Component searchResultsList = createSearchResultsList();
        createFacetComponent();
        add(facets, searchResultsList);
        expand(searchResultsList);
    }

    /**
     * Prepare QueryParameters
     *
     * @param query  query string
     * @param otherParams request parameters e.g. media, reusability
     * @return QueryParameters used by the search page
     */
    public static QueryParameters prepareQueryParameters(int aggregatorId, String query, Map<String, String> otherParams) {
        Map<String, List<String>> parameters = new HashMap<>();
        addParameter(AGGREGATOR_PARAM_NAME, String.valueOf(aggregatorId), parameters);
        addParameter(QUERY_PARAM_NAME, query, parameters);
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
        facets = FacetComponentFactory.getFacetComponent(aggregator.getValue().getId(), this);
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
     * @return created component
     */
    private Component createSearchResultsList() {
        VerticalLayout searchResultsList = new VerticalLayout();
        createAggregatorSelectionBox();
        searchResultsList.add(aggregator);
        searchResultsList.add(createQueryForm());
        createSearchOnlyIiifBox();
        searchResultsList.add(searchOnlyIiif);
        createNoResultsLabel();
        searchResultsList.add(noResults);
        resultsComponent = new SearchResultsComponent(this, currentUserRecordSelection);
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

    private void createAggregatorSelectionBox() {
        aggregator = new Select<>();
        aggregator.addClassName("aggregator-selector");
        aggregator.setItems(Arrays.stream(Aggregator.values()).filter(a -> a != Aggregator.UNKNOWN));
        aggregator.setValue(Aggregator.getById(0));
        currentUserRecordSelection.setAggregator(Aggregator.values()[0]);
        aggregator.setLabel("Available aggregators");
        aggregator.setEmptySelectionAllowed(false);
        aggregator.addValueChangeListener(event -> {
            if (!currentUserRecordSelection.getSelectedRecordIds().isEmpty()) {
                ConfirmationDialog dialog = new ConfirmationDialog("Not added records",
                        "There are " + currentUserRecordSelection.getSelectedRecordIds().size()
                                + " selected but not added record(s). Record selection will be lost when aggregator is changed.",
                        e -> handleAggregatorChange(event.getValue()));
                dialog.addContent("Are you sure you want to continue?");
                dialog.open();
            } else {
                handleAggregatorChange(event.getValue());
            }
        });
    }

    private void handleAggregatorChange(Aggregator aggregator) {
        currentUserRecordSelection.clearSelectedRecords();
        currentUserRecordSelection.setAggregator(aggregator);
        resultsComponent.clear();
        facets = FacetComponentFactory.getFacetComponent(aggregator.getId(), this);
        facets.addFacets(null);
        showFacets(false);
        search.setPlaceholder("Search in " + aggregator.getFullName());
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
            Project project = event.getValue();
            datasets.setItems(project.getDatasets());
            currentUserRecordSelection.setSelectedProject(project);
        });
        projects.setValue(currentProject);
        //
        datasets.setItems(currentProject.getDatasets());
        datasets.setLabel("Available datasets");
        datasets.setEmptySelectionAllowed(true);
        datasets.addValueChangeListener(event -> {
            Dataset selectedDataset = event.getValue();
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
					ui.access(() -> resultsComponent.deselectAll());
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
        search.setPlaceholder("Search in " + aggregator.getValue().getFullName());
        search.setAutofocus(true);
        search.addKeyUpListener(Key.ENTER, keyUpEvent -> search.getUI().ifPresent(this::navigateToSearch));
        Button searchButton = new Button();
        searchButton.setIcon(new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(e -> e.getSource().getUI().ifPresent(this::navigateToSearch));
        queryForm.add(search, searchButton);
        queryForm.expand(search);
        queryForm.setDefaultVerticalComponentAlignment(Alignment.START);
        queryForm.expand();
        return queryForm;
    }

    private void navigateToSearch(UI ui) {
        HashMap<String, String> otherParams = new HashMap<>(facets.getDefaultFacets());
        ui.navigate("search", prepareQueryParameters(aggregator.getValue().getId(), search.getValue(), otherParams));
    }

    private void search(int aggregatorId, String query,  Map<String, String> requestParams) {
        if (!currentUserRecordSelection.getSelectedRecordIds().isEmpty()) {
            ConfirmationDialog dialog = new ConfirmationDialog("Not added records",
                    "There are " + currentUserRecordSelection.getSelectedRecordIds().size()
                            + " selected but not added record(s). Record selection will be lost with next search query execution.",
                    e -> executeSearch(aggregatorId, query, requestParams));
            dialog.addContent("Are you sure you want to continue?");
            dialog.open();
        } else {
            executeSearch(aggregatorId, query, requestParams);
        }
    }

    /**
     * Execute search in the SearchResultsComponent and add facets
     *
     * @param query  query string
     * @param requestParams request parameters e.g. media, reusability
     */
    private void executeSearch(int aggregatorId, String query, Map<String, String> requestParams) {
		currentUserRecordSelection.clearSelectedRecords();
        if (query == null || query.isEmpty()) {
            resultsComponent.clear();
            facets.addFacets(null);
            showFacets(false);
            search.clear();
            this.query = null;
            this.requestParams = null;
        } else {
            if (search.isEmpty()) {
                search.setValue(query);
            }
            this.query = query;
            this.requestParams = requestParams;
            SearchResults searchResults = searchService.search(aggregatorId, query, requestParams);

            if (searchResults != null) {
                resultsComponent.handleSearchResults(searchResults);
                fillMissingValuesAndVerifyResult(searchResults);
                facets.addFacets(searchResults.getFacets());
                facets.updateState(requestParams);
                showFacets(true);
                noResults.setVisible(searchResults.getTotalResults() == 0);
                invertSelectionButton.setVisible(searchResults.getTotalResults() > 0);
                selectAllButton.setVisible(searchResults.getTotalResults() > 0);
                addElementsButton.setVisible(searchResults.getTotalResults() > 0);
            } else {
                Notification.show("Search failed!", 3000, Notification.Position.MIDDLE);
            }
        }
    }

    public void fillMissingValuesAndVerifyResult(SearchResults searchResults) {
        int aggregatorId = aggregator.getValue().getId();
        List<SearchResult> results = searchResults.getResults();
        UI ui = UI.getCurrent();

        results.forEach(result -> CompletableFuture.supplyAsync(() -> searchResponseFillerService.fillMissingDataAndValidate(aggregatorId, result))
                .thenAccept(r -> resultsComponent.updateSearchResult(ui, r)));
    }

    /**
     * Execute facet search after a facet was selected or deselected. The current query string is used and the pagination is
     * set as for the first execution.
     */
    public void executeFacetSearch(Map<String, String> requestParams) {
        getUI().ifPresent(ui -> ui.navigate("search", SearchPage.prepareQueryParameters(aggregator.getValue().getId(), query, requestParams)));
    }

    /**
     * Execute search after result page was selected. Current query string and request parameters are used.
     *
     * @param paginationParams pagination request parameters
     * @return SearchResponse object if search operation finish successfully, null otherwise.
     */
    public SearchResults goToPage(Map<String, String> paginationParams) { //todo cache? here or in search service
        if (requestParams != null) {
            requestParams.putAll(paginationParams);
            return searchService.search(aggregator.getValue().getId(), query, requestParams);
        }
        return null;
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
        if (queryParameters != null && !queryParameters.getParameters().isEmpty()) {
            Map<String, List<String>> parametersMap = queryParameters.getParameters();
            String aggregatorParamValue = getParameterValue(parametersMap.get(AGGREGATOR_PARAM_NAME), true);
            boolean valid = Aggregator.isValid(aggregatorParamValue);
            if (!valid) {
            	if (aggregatorParamValue != null) {
					Notification.show("Unknown/Invalid aggregator!", 4000, Notification.Position.TOP_CENTER);
				}
                return;
            }
            int aggregatorId = Integer.parseInt(aggregatorParamValue);
            aggregator.setValue(Aggregator.getById(aggregatorId));
            currentUserRecordSelection.setAggregator(Aggregator.getById(aggregatorId));

            String queryString = getParameterValue(parametersMap.get(QUERY_PARAM_NAME), true);

            String onlyIiifParam = getParameterValue(parametersMap.get(ONLY_IIIF_PARAM_NAME), true);
            setOnlyIiif(onlyIiifParam == null || Boolean.parseBoolean(onlyIiifParam));
            searchOnlyIiif.setValue(onlyIiif);

            Map<String, String> requestParams = new HashMap<>();
            parametersMap.entrySet().stream()
                    .filter(e -> !(e.getKey().equalsIgnoreCase(AGGREGATOR_PARAM_NAME) || e.getKey().equalsIgnoreCase(QUERY_PARAM_NAME)))
                    .forEach(e -> requestParams.put(e.getKey(), e.getValue().get(0)));
            search(aggregatorId, queryString, requestParams);
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
		searchResponseFillerService.clearCache();
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
