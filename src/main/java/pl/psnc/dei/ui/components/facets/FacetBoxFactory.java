package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.response.search.ddb.DDBFacet;
import pl.psnc.dei.response.search.europeana.EuropeanaFacet;
import pl.psnc.dei.response.search.Facet;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class FacetBoxFactory {

	public static FacetBox createFacetBox(FacetComponent parent, Facet facet) {
		if (facet instanceof EuropeanaFacet) {
			return new EuropeanaFacetBox(parent, facet);
		} else if(facet instanceof DDBFacet) {
			return new DDBFacetBox(parent, facet);
		} else {
			throw new NotImplementedException();
		}
	}
}
