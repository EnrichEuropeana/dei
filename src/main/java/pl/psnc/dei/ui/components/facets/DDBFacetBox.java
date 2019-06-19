package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;

import static pl.psnc.dei.util.DDBConstants.FACET_LABELS;

public class DDBFacetBox extends FacetBox {
	public DDBFacetBox(FacetComponent parent, Facet<FacetField> facet) {
		super(parent, facet);
	}

	@Override
	protected String getFacetLabelText() {
		return FACET_LABELS.get(this.getFacet());
	}
}
