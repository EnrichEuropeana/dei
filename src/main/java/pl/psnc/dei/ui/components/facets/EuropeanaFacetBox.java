package pl.psnc.dei.ui.components.facets;

import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;

import java.util.HashMap;
import java.util.Map;

public class EuropeanaFacetBox extends FacetBox {

	// Labels for facet fields
	private static final Map<String, String> FACET_LABELS;

	static {
		FACET_LABELS = new HashMap<>();
		FACET_LABELS.put("YEAR", "Year");
		FACET_LABELS.put("RIGHTS", "Rights");
		FACET_LABELS.put("DATA_PROVIDER", "Data provider");
		FACET_LABELS.put("PROVIDER", "Provider");
		FACET_LABELS.put("COLOURPALETTE", "Colour palette");
		FACET_LABELS.put("COUNTRY", "Country");
		FACET_LABELS.put("LANGUAGE", "Language");
		FACET_LABELS.put("MIME_TYPE", "Mime type");
		FACET_LABELS.put("TYPE", "Type");
		FACET_LABELS.put("IMAGE_SIZE", "Image size");
		FACET_LABELS.put("SOUND_DURATION", "Sound duration");
		FACET_LABELS.put("REUSABILITY", "Reusability");
		FACET_LABELS.put("VIDEO_DURATION", "Video duration");
		FACET_LABELS.put("TEXT_FULLTEXT", "Has fulltext");
		FACET_LABELS.put("LANDINGPAGE", "Landing page");
		FACET_LABELS.put("MEDIA", "Media");
		FACET_LABELS.put("THUMBNAIL", "Thumbnail");
		FACET_LABELS.put("UGC", "UGC");
		FACET_LABELS.put("IMAGE_ASPECTRATIO", "Image aspect ratio");
		FACET_LABELS.put("IMAGE_COLOUR", "Image colour");
		FACET_LABELS.put("VIDEO_HD", "Video HD");
		FACET_LABELS.put("SOUND_HQ", "Sound HQ");
	}

	public EuropeanaFacetBox(FacetComponent parent, Facet<FacetField> facet) {
		super(parent, facet);
	}

	@Override
	protected String getFacetLabelText() {
		return FACET_LABELS.get(this.getFacet());
	}
}
