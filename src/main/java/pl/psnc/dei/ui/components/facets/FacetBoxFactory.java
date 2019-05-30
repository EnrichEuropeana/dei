package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.response.search.EuropeanaFacet;
import pl.psnc.dei.response.search.Facet;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class FacetBoxFactory {

	public static FacetBox createFacetBox(FacetComponent parent, Facet facet) {
		if (facet instanceof EuropeanaFacet) {
			return new EuropeanaFacetBox(parent, facet);
		} else { //todo implement DDB facetBox
			throw new NotImplementedException();
		}
	}
}
