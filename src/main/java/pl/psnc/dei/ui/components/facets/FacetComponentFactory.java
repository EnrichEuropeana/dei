package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.ui.pages.SearchPage;

public class FacetComponentFactory {

	public static FacetComponent getFacetComponent(int aggregatorId, SearchPage searchPage) {
		Aggregator aggregator = Aggregator.getById(aggregatorId);

		switch (aggregator) {
			case EUROPEANA:
				return new EuropeanaFacetComponent(searchPage);
			case DDB:
				return new DDBFacetComponent(searchPage);
			default:
				throw new IllegalArgumentException("Invalid / Unknown aggregator");
		}
	}
}
