package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.response.search.FacetField;
import pl.psnc.dei.response.search.europeana.EuropeanaFacet;
import pl.psnc.dei.response.search.Facet;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
		} else { //todo implement DDB facetBox
			throw new NotImplementedException();
		}
	}

	private static boolean isBooleanFacet(Facet<FacetField> facet) {
		List<FacetField> fields = facet.getFields();
		if (fields.size() == 2) {
			boolean truePresent = false;
			boolean falsePresent = false;

			for (FacetField field : fields) {
				if(field.getLabel().equalsIgnoreCase("true")) {
					truePresent = true;
				} else if(field.getLabel().equalsIgnoreCase("false")) {
					falsePresent = true;
				}
			}
			return truePresent && falsePresent;
		}
		return false;
	}
}
