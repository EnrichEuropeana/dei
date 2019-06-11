package pl.psnc.dei.ui.components.facets;

import com.vaadin.flow.component.checkbox.Checkbox;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;

import java.util.List;

public class EuropeanaBooleanFacetBox extends EuropeanaFacetBox {

	public EuropeanaBooleanFacetBox(FacetComponent parent, Facet<FacetField> facet) {
		super(parent, facet);
	}

	@Override
	protected void handleFacetField(Checkbox fieldCheckbox) {
		facetComponent.clearValuesForFacet(facet);
		super.handleFacetField(fieldCheckbox);
	}

	@Override
	public void updateFacets(List<String> selectedValues) {
		if (selectedValues != null && !selectedValues.isEmpty()) {
			String firstValue = selectedValues.get(0);
			values.forEach((key, value) -> {
				key.setValue(firstValue.equals(value.getLabel()));
			});
		}
	}
}
