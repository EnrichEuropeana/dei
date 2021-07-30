package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;
import pl.psnc.dei.response.search.ddb.DDBFacet;
import pl.psnc.dei.response.search.europeana.EuropeanaFacet;

import java.util.List;

public class FacetBoxFactory {

	@SuppressWarnings("unchecked")
	public static FacetBox createFacetBox(FacetComponent parent, Facet facet) {
		if (facet instanceof EuropeanaFacet) {
			if (isBooleanFacet(facet)) {
				return new EuropeanaBooleanFacetBox(parent, facet);
			} else {
				return new EuropeanaFacetBox(parent, facet);
			}
		} else if(facet instanceof DDBFacet) {
			return new DDBFacetBox(parent, facet);
		} else {
			throw new UnsupportedOperationException("Unsupported facet type");
		}
	}

	private static boolean isBooleanFacet(Facet<FacetField> facet) {
		List<FacetField> fields = facet.getFields();
		return fields.size() == 2
				&& fields.stream().anyMatch(e -> e.getLabel().equalsIgnoreCase("true"))
				&& fields.stream().anyMatch(e -> e.getLabel().equalsIgnoreCase("false"));
	}
}
