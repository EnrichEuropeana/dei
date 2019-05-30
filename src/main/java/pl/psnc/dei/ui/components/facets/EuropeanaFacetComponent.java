package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.schema.search.EuropeanaCursorPagination;
import pl.psnc.dei.ui.pages.SearchPage;

import java.util.*;
import java.util.stream.Collectors;

import static pl.psnc.dei.ui.pages.SearchPage.ONLY_IIIF_PARAM_NAME;

public class EuropeanaFacetComponent extends FacetComponent {

	public static final String QF_PARAM_NAME = "qf";

	private static final Map<String, String> EUROPEANA_DEFAULT_FACETS = new HashMap<>();

	private static final String[] EUROPEANA_PARAM_FACETS = {"COLOURPALETTE", "LANDINGPAGE", "MEDIA", "REUSABILITY", "TEXT_FULLTEXT", "THUMBNAIL"};

	static {
		EUROPEANA_DEFAULT_FACETS.put("MEDIA", "true");
		EUROPEANA_DEFAULT_FACETS.put("REUSABILITY", "open");
	}

	// Filter query from facets
	private Map<String, List<String>> fq = new HashMap<>();

	// Facets that are separate request params
	private Map<String, List<String>> facetParams = new HashMap<>();

	public EuropeanaFacetComponent(SearchPage searchPage) {
		super(searchPage);
	}

	private void handleEuropeanaFacet(String facet, String facetValue, boolean add, Map<String, List<String>> facets) {
		if (add) {
			facets.computeIfAbsent(facet, k -> new ArrayList<>()).add(facetValue);
		} else {
			if (facets.containsKey(facet)) {
				facets.get(facet).remove(facetValue);
				if (facets.get(facet).isEmpty()) {
					facets.remove(facet);
				}
			}
		}
	}

	/**
	 * Prepare query filter where all selected values for the same facet are joined with OR and selections from different facets
	 * are joined with AND
	 *
	 * @return query filter string like this: ((f1:v1) OR (f1:v2) ... OR (f1:vN)) AND ((f2:v1) OR (f2:v2) ... OR (f2:vN)) ... AND ((fM:v1) OR (fM:v2) ... OR (fM:vN))
	 */
	private String prepareQueryFilter() {
		List<String> queryValues = new ArrayList<>();

		fq.forEach((s, strings) -> {
			String queryValue = strings.size() == 1
					? s + ":" + "\"" + strings.get(0) + "\""
					: strings.stream().map(v -> "(" + s + ":" + "\"" + v + "\"" + ")").collect(Collectors.joining(" OR "));
			queryValues.add(queryValue);
		});
		if (queryValues.size() == 1) {
			return queryValues.get(0);
		}
		return queryValues.stream().map(s -> "(" + s + ")").collect(Collectors.joining(" AND "));
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
	 * Select the necessary facet checkboxes based on the request parameters
	 *
	 * @param requestParams request parameters
	 */
	private void handleQueryFilterString(Map<String, String> requestParams) {
		String qf = requestParams.get(QF_PARAM_NAME);

		if (qf != null && !qf.isEmpty()) {
			fq.clear();

			List<String> filterQueries = Arrays.asList(qf.split(" AND "));
			filterQueries.stream().map(String::trim).map(s -> removeTrailing(s, "(")).
					map(s -> removeTrailing(s, ")")).forEach(s -> {
				List<String> filterValues = Arrays.asList(s.split(" OR "));
				filterValues.stream().map(String::trim).map(f -> removeTrailing(f, "(")).
						map(f -> removeTrailing(f, ")")).forEach(f -> {
					int pos = f.indexOf(':');
					if (pos != -1) {
						fq.computeIfAbsent(f.substring(0, pos), v -> new ArrayList<>()).add(removeTrailing(f.substring(pos + 1), "\""));
					}
				});
			});
			if (!fq.isEmpty()) {
				fq.keySet().forEach(s -> {
					facetBoxes.stream().filter(facetBox -> facetBox.getFacet().equals(s)).forEach(facetBox -> facetBox.updateFacets(fq.get(s)));
					selectedFacetsComponent.addSelectedValues(s, fq.get(s));
				});
			}
		} else {
			fq.clear();
		}
	}

	/**
	 * Select the necessary facet checkboxes based on the request parameters collection
	 *
	 * @param requestParams request parameters collection
	 */
	private void handleRequestParams(Map<String, String> requestParams) {
		if (requestParams != null && !requestParams.isEmpty()) {
			facetParams.clear();

			List<String> paramsToSkip = getParamsToSkip();
			requestParams.entrySet().stream()
					.filter(e -> !paramsToSkip.contains(e.getKey()))
					.forEach(e -> {
						List<String> strings = Arrays.asList(e.getValue().split(","));
						facetParams.computeIfAbsent(e.getKey().toUpperCase(), k -> new ArrayList<>()).addAll(strings);
					});

			if (!facetParams.isEmpty()) {
				facetParams.keySet().forEach(s -> {
					facetBoxes.stream().filter(facetBox -> facetBox.getFacet().equalsIgnoreCase(s)).forEach(facetBox -> facetBox.updateFacets(facetParams.get(s)));
					selectedFacetsComponent.addSelectedValues(s, facetParams.get(s));
				});
			}
		} else {
			facetParams.clear();
		}
	}

	private List<String> getParamsToSkip() {
		String[] paginationParamsNames = EuropeanaCursorPagination.getRequestParamsNames();

		List<String> toSkip = new ArrayList<>(Arrays.asList(paginationParamsNames));
		toSkip.add(QF_PARAM_NAME);
		toSkip.add(ONLY_IIIF_PARAM_NAME);

		return toSkip;
	}

	/**
	 * Helper method for removing the trailing brackets
	 *
	 * @param filterQuery query to be changed
	 * @return the given filter query without trailing brackets
	 */
	private String removeTrailing(String filterQuery, String toRemove) {
		if (filterQuery.startsWith(toRemove)) {
			filterQuery = filterQuery.substring(toRemove.length());
		}
		if (filterQuery.endsWith(toRemove)) {
			filterQuery = filterQuery.substring(0, filterQuery.length() - toRemove.length());
		}
		return filterQuery;
	}

	@Override
	public void executeFacetSearch(String facet, String facetValue, boolean add) {
		if (Arrays.asList(EUROPEANA_PARAM_FACETS).contains(facet.toUpperCase())) {
			handleEuropeanaFacet(facet, facetValue, add, facetParams);
		} else {
			handleEuropeanaFacet(facet, facetValue, add, fq);
		}
		Map<String, String> requestParams = prepareRequestParams();
		requestParams.put(QF_PARAM_NAME, prepareQueryFilter());
		searchPage.executeFacetSearch(requestParams);
	}

	@Override
	public void updateState(Map<String, String> requestParams) {
		handleQueryFilterString(requestParams);
		handleRequestParams(requestParams);

		if (requestParams.isEmpty()) {
			selectedFacetsComponent.clear();
		}
	}

	@Override
	public Map<String, String> getDefaultFacets() {
		return EUROPEANA_DEFAULT_FACETS;
	}
}
