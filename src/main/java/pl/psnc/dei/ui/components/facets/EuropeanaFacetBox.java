package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;

import static pl.psnc.dei.util.EuropeanaConstants.FACET_LABELS;

public class EuropeanaFacetBox extends FacetBox {

	public EuropeanaFacetBox(FacetComponent parent, Facet<FacetField> facet) {
		super(parent, facet);
	}

	@Override
	protected String getFacetLabelText() {
		return FACET_LABELS.get(this.getFacet());
	}
}
