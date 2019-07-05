package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.util.IiifAvailability;

/**
 * Single result component
 */
public class SearchResultEntryComponent extends HorizontalLayout {

	private static final String AUTHOR_LABEL = "Author:";
	private static final String TITLE_LABEL = "Title:";
	private static final String ISSUED_LABEL = "Issued:";
	private static final String PROVIDER_LABEL = "Provider institution:";
	private static final String FORMAT_LABEL = "Format:";
	private static final String LOADING_PLACEHOLDER_LABEL = "Loading...";
	private static final String LANGUAGE_LABEL = "Language:";
	private static final String LICENSE_LABEL = "License:";
	private static final String IIIF_AVAILABILITY_LABEL = "IIIF availability:";
	private static final String IIIF_AVAILABILITY_PLACEHOLDER_LABEL = "Verifying...";

	private Checkbox searchResultCheckBox;
	private VerticalLayout metadata;

	private CurrentUserRecordSelection currentUserRecordSelection;

	private SearchResult searchResult;

	private boolean recordEnabled = false;

	public SearchResultEntryComponent(CurrentUserRecordSelection currentUserRecordSelection,
									  SearchResult searchResult) {
		this.currentUserRecordSelection = currentUserRecordSelection;
		this.searchResult = searchResult;

		addClassName("search-result-element");
		setSizeFull();
		setDefaultVerticalComponentAlignment(Alignment.CENTER);
		createSearchResultCheckBox();
		createImage();
		metadata = createMetadataComponent();
		setRecordEnabled(false);
		add(metadata);
	}

	/**
	 * Updates metadata section of result component
	 *
	 * @param searchResult updated searchResult object
	 */
	public void updateMetadata(SearchResult searchResult) {
		this.searchResult = searchResult;
		VerticalLayout updatedMetadata = createMetadataComponent();
		if (searchResult.getIiifAvailability() != null) {
			setRecordEnabled(searchResult.getIiifAvailability().isTransferPossible());
		}

		replace(metadata, updatedMetadata);
		metadata = updatedMetadata;
	}

	/**
	 * If true selects record, otherwise deselects record
	 *
	 * @param isSelected value to set
	 */
	public void setRecordSelected(boolean isSelected) {
		searchResultCheckBox.setValue(isSelected);
		if (!isSelected) {
			currentUserRecordSelection.removeSelectedRecordId(searchResult.getId());
		}
	}

	/**
	 * Inverts record selection
	 */
	public void invertRecordSelection() {
		setRecordSelected(!isRecordSelected());
	}

	/**
	 * Checks if record is selected
	 *
	 * @return true if record is selected, false otherwise
	 */
	public boolean isRecordSelected() {
		return searchResultCheckBox.getValue();
	}

	/**
	 * Creates checkbox component
	 */
	private void createSearchResultCheckBox() {
		searchResultCheckBox = new Checkbox();
		searchResultCheckBox.setId(searchResult.getId());
		searchResultCheckBox.addClassName("search-result-checkbox");

		boolean isSelected = currentUserRecordSelection.isRecordSelected(searchResult.getId());
		searchResultCheckBox.setValue(isSelected);

		searchResultCheckBox.addValueChangeListener(event -> {
			if (event.getValue() && !event.getOldValue()) {
				currentUserRecordSelection.addSelectedRecord(new Record(searchResult.getId(), searchResult.getTitle()));
			} else {
				currentUserRecordSelection.removeSelectedRecordId(event.getSource().getId().get());
			}
		});
		add(searchResultCheckBox);
	}

	/**
	 * Create thumbnail image
	 */
	private void createImage() {
		VerticalLayout thumbnailContainer = new VerticalLayout();
		thumbnailContainer.addClassName("metadata-image-container");
		thumbnailContainer.setAlignItems(Alignment.CENTER);

		if(searchResult.getImageURL() != null) {
			Image thumbnail = new Image();
			thumbnail.setAlt(searchResult.getTitle());
			thumbnail.setSrc(searchResult.getImageURL());
			thumbnail.addClassName("metadata-image");
			addSourceObjectUrl(thumbnailContainer, thumbnail);
		} else {
			Icon icon = new Icon(VaadinIcon.FILE_PICTURE);
			icon.addClassName("metadata-image");
			icon.setSize("100px");
			icon.setColor("darkgray");
			thumbnailContainer.add(icon);
			addSourceObjectUrl(thumbnailContainer, icon);
		}
		add(thumbnailContainer);
	}

	/**
	 * If possible adds link to source object to thumbnail.
	 *
	 * @param thumbnailContainer thumbnail container object
	 * @param thumbnail thumbnail object, e.g. image or placeholder icon
	 */
	private void addSourceObjectUrl(VerticalLayout thumbnailContainer, Component thumbnail) {
		String sourceObjectURL = searchResult.getSourceObjectURL();
		if (sourceObjectURL != null && !sourceObjectURL.isEmpty()) {
			Anchor link = new Anchor(sourceObjectURL, thumbnail);
			link.setTarget("_blank");
			thumbnailContainer.add(link);
		} else {
			thumbnailContainer.add(thumbnail);
		}
	}

	/**
	 * Create metadata component which is part of the result component
	 */
	private VerticalLayout createMetadataComponent() {
		VerticalLayout metadataLayout = new VerticalLayout();
		createTitleMetadataLine(metadataLayout, searchResult.getTitle(), searchResult.getSourceObjectURL());
		if (searchResult.getAuthor() != null) {
			createMetadataLine(metadataLayout, AUTHOR_LABEL, searchResult.getAuthor());
		} else {
			createMetadataLine(metadataLayout, AUTHOR_LABEL, LOADING_PLACEHOLDER_LABEL);
		}

		createMetadataLine(metadataLayout, ISSUED_LABEL, searchResult.getIssued()); //todo never used?

		if (searchResult.getProvider() != null) {
			createMetadataLine(metadataLayout, PROVIDER_LABEL, searchResult.getProvider());
		} else {
			createMetadataLine(metadataLayout, PROVIDER_LABEL, LOADING_PLACEHOLDER_LABEL);
		}

		if (searchResult.getFormat() != null) {
			createMetadataLine(metadataLayout, FORMAT_LABEL, searchResult.getFormat());
		} else {
			createMetadataLine(metadataLayout, FORMAT_LABEL, LOADING_PLACEHOLDER_LABEL);
		}

		if (searchResult.getLanguage() != null) {
			createMetadataLine(metadataLayout, LANGUAGE_LABEL, searchResult.getLanguage());
		} else {
			createMetadataLine(metadataLayout, LANGUAGE_LABEL, LOADING_PLACEHOLDER_LABEL);
		}

		if (searchResult.getLicense() != null) {
			createMetadataLine(metadataLayout, LICENSE_LABEL, searchResult.getLicense());
		} else {
			createMetadataLine(metadataLayout, LICENSE_LABEL, LOADING_PLACEHOLDER_LABEL);
		}

		IiifAvailability iiifAvailability = searchResult.getIiifAvailability();
		if (iiifAvailability != null) {
			createTransferPossibilityLine(metadataLayout, iiifAvailability.getMessage(), iiifAvailability.isTransferPossible());
		} else {
			createMetadataLine(metadataLayout, IIIF_AVAILABILITY_LABEL, IIIF_AVAILABILITY_PLACEHOLDER_LABEL);
		}

		return metadataLayout;
	}

	/**
	 * Creates a single line of the title metadata component. If possible title label will redirect to object on
	 * aggregator portal
	 *
	 * @param metadata        metadata component the line will be added to
	 * @param value           record title
	 * @param sourceObjectURL URL to the object on aggregator portal
	 */
	private void createTitleMetadataLine(FlexComponent metadata, String value, String sourceObjectURL) {
		if (value != null && !value.isEmpty()) {
			if (sourceObjectURL != null && !sourceObjectURL.isEmpty()) {
				HorizontalLayout line = createLineWithMetadataLabel(TITLE_LABEL);
				Label valueLabel = new Label(value);
				Anchor link = new Anchor(sourceObjectURL, valueLabel);
				link.setTarget("_blank");
				line.add(link);
				line.expand(link);
				metadata.add(line);
			} else {
				createMetadataLine(metadata, TITLE_LABEL, value);
			}
		}
	}

	/**
	 * Create a single line of the metadata component
	 *
	 * @param metadata metadata component where the line will be added
	 * @param label    label of the attribute
	 * @param value    value of the attribute
	 */
	private void createMetadataLine(FlexComponent metadata, String label, String value) {
		if (value != null && !value.isEmpty()) {
			HorizontalLayout line = createLine(label, value);
			metadata.add(line);
		}
	}

	private HorizontalLayout createLine(String label, String value) {
		HorizontalLayout line = createLineWithMetadataLabel(label);
		Label valueLabel = new Label(value);
		line.add(valueLabel);
		line.expand(valueLabel);
		return line;
	}

	private HorizontalLayout createLineWithMetadataLabel(String label) {
		HorizontalLayout line = new HorizontalLayout();
		line.addClassName("metadata-line");
		line.setPadding(false);
		line.setVerticalComponentAlignment(Alignment.START);
		Label name = new Label(label);
		name.addClassName("metadata-label");
		line.add(name);
		return line;
	}

	private void createTransferPossibilityLine(FlexComponent metadata, String message, boolean transferPossible) {
		HorizontalLayout line = createLineWithMetadataLabel(IIIF_AVAILABILITY_LABEL);
		Label valueLabel = new Label(message);
		if (transferPossible) {
			valueLabel.addClassName("can-transfer-label");
		} else {
			valueLabel.addClassName("cannot-transfer-label");
		}
		line.add(valueLabel);
		line.expand(valueLabel);
		metadata.add(line);
	}

	public void setRecordEnabled(boolean isEnabled) {
		this.recordEnabled = isEnabled;
		searchResultCheckBox.setEnabled(isEnabled);
		if (isEnabled) {
			removeClassName("search-result-element-disabled");
			addClassName("search-result-element-enabled");
		} else {
			removeClassName("search-result-element-enabled");
			addClassName("search-result-element-disabled");
		}
	}

	public boolean isRecordEnabled() {
		return recordEnabled;
	}

	public String getRecordId() {
		return searchResult.getId();
	}
}
