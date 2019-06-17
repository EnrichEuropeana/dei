package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.ui.pages.SearchPage;

import java.util.*;

import static pl.psnc.dei.util.DDBConstants.FACET_LABELS;
import static pl.psnc.dei.util.DDBConstants.FACET_VALUES_NAMES;

public class DDBFacetComponent extends FacetComponent {

	private Map<String, List<String>> facetParams = new HashMap<>();

	public DDBFacetComponent(SearchPage searchPage) {
		super(searchPage);
	}

	/**
	 * Prepare request parameters where all selected values for the same facet are joined with comma
	 *
	 * @return request parameters collection
	 */
	private Map<String, String> prepareRequestParams() {
		Map<String, String> requestParams = new HashMap<>();

		facetParams.forEach((s, strings) -> requestParams.put(s, String.join(",", strings)));

		return requestParams;
	}

	/**
	 * Select the necessary facet checkboxes based on the request parameters collection
	 *
	 * @param requestParams request parameters collection
	 */
	private void handleRequestParams(Map<String, String> requestParams) {
		if (requestParams != null && !requestParams.isEmpty()) {
			facetParams.clear();

			requestParams.entrySet().stream()
					.filter(e -> FACET_LABELS.keySet().contains(e.getKey()))
					.forEach(e -> {
						List<String> strings = Arrays.asList(e.getValue().split(","));
						facetParams.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(strings);
					});

			if (!facetParams.isEmpty()) {
				facetParams.keySet().forEach(s -> {
					facetBoxes.stream().filter(facetBox -> facetBox.getFacet().equalsIgnoreCase(s)).forEach(facetBox -> facetBox.updateFacets(facetParams.get(s)));
					HashMap<String, String> valuesToDisplay = new HashMap<>();
					facetParams.get(s).forEach(v -> {
						String displayName = FACET_VALUES_NAMES.getOrDefault(v, v);
						valuesToDisplay.put(v, displayName);
					});
					selectedFacetsComponent.addSelectedValues(s, valuesToDisplay);
				});
			}
		} else {
			facetParams.clear();
		}
	}

	@Override
	public void executeFacetSearch(String facet, String facetValue, boolean add) {
		if (add) {
			facetParams.computeIfAbsent(facet, k -> new ArrayList<>()).add(facetValue);
		} else {
			if (facetParams.containsKey(facet)) {
				facetParams.get(facet).remove(facetValue);
				if (facetParams.get(facet).isEmpty()) {
					facetParams.remove(facet);
				}
			}
		}

		Map<String, String> requestParams = prepareRequestParams();
		searchPage.executeFacetSearch(requestParams);
	}

	@Override
	public void updateState(Map<String, String> requestParams) {
		selectedFacetsComponent.clear();
		handleRequestParams(requestParams);
	}

	@Override
	public Map<String, String> getDefaultFacets() {
		return new HashMap<>();
	}

	@Override
	public void clearValuesForFacet(String facet) {
		facetParams.remove(facet);
	}

	@Override
	public Map<String, String> getFacetsLabels() {
		return FACET_LABELS;
	}
}
