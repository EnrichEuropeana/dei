package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.ui.pages.SearchPage;

import java.util.*;
import java.util.stream.Collectors;

import static pl.psnc.dei.util.EuropeanaConstants.*;

public class DDBFacetComponent extends FacetComponent {

	// Filter query from facets
	private Map<String, List<String>> fq = new HashMap<>();

	// Facets that are separate request params
	private Map<String, List<String>> facetParams = new HashMap<>();

	public DDBFacetComponent(SearchPage searchPage) {
		super(searchPage);
	}



	@Override
	public void executeFacetSearch(String facet, String facetValue, boolean add) {
//		if (Arrays.asList(PARAM_FACETS).contains(facet.toUpperCase())) {
//			handleEuropeanaFacet(facet, facetValue, add, facetParams);
//		} else {
//			handleEuropeanaFacet(facet, facetValue, add, fq);
//		}
//		Map<String, String> requestParams = prepareRequestParams();
//		requestParams.put(QF_PARAM_NAME, prepareQueryFilter());
//		searchPage.executeFacetSearch(requestParams);
	}

	@Override
	public void updateState(Map<String, String> requestParams) {
//		handleQueryFilterString(requestParams);
//		handleRequestParams(requestParams);
//
//		if (requestParams.isEmpty()) {
//			selectedFacetsComponent.clear();
//		}
	}

	@Override
	public Map<String, String> getDefaultFacets() {
		return new HashMap<>();
	}
}
