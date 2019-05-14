package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.model.CurrentUserRecordSelection;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.service.UIPollingManager;
import pl.psnc.dei.util.RecordTransferValidationUtil;

import java.util.concurrent.CompletableFuture;

/**
 * Single result component
 */
public class SearchResultEntryComponent extends HorizontalLayout {

	private static final String AUTHOR_LABEL = "Author:";
	private static final String TITLE_LABEL = "Title:";
	private static final String ISSUED_LABEL = "Issued:";
	private static final String PROVIDER_LABEL = "Provider institution:";
	private static final String FORMAT_LABEL = "Format:";
	private static final String FORMAT_LOADING_PLACEHOLDER_LABEL = "Loading...";
	private static final String LANGUAGE_LABEL = "Language:";
	private static final String LICENSE_LABEL = "License:";
	private static final String TRANSFER_POSSIBILITY_LABEL = "Transfer possibility:";
	private static final String TRANSFER_POSSIBILITY_PLACEHOLDER_LABEL = "Verifying...";

	private Checkbox searchResultCheckBox;
	private Image thumbnail;
	private HorizontalLayout transferPossibilityLine;

	private CurrentUserRecordSelection currentUserRecordSelection;

	private EuropeanaRestService europeanaRestService;

	private UIPollingManager uiPollingManager;

	private RecordTransferValidationCache recordTransferValidationCache;

	private SearchResult searchResult;

	private boolean recordEnabled = false;

	public SearchResultEntryComponent(CurrentUserRecordSelection currentUserRecordSelection,
									  EuropeanaRestService europeanaRestService,
									  UIPollingManager uiPollingManager,
									  RecordTransferValidationCache recordTransferValidationCache,
									  SearchResult searchResult) {
		this.currentUserRecordSelection = currentUserRecordSelection;
		this.europeanaRestService = europeanaRestService;
		this.uiPollingManager = uiPollingManager;
		this.recordTransferValidationCache = recordTransferValidationCache;
		this.searchResult = searchResult;

		addClassName("search-result-element");
		setSizeFull();
		setDefaultVerticalComponentAlignment(Alignment.CENTER);
		createSearchResultCheckBox();
		createImage();
		createMetadataComponent();
	}

	/**
	 * If true selects record, otherwise deselects record
	 *
	 * @param isSelected value to set
	 */
	public void setRecordSelected(boolean isSelected) {
		searchResultCheckBox.setValue(isSelected);
		if (isSelected) {
			currentUserRecordSelection.addSelectedRecordId(searchResult.getId());
		} else {
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
		searchResultCheckBox.setEnabled(false);

		boolean isSelected = currentUserRecordSelection.isRecordSelected(searchResult.getId());
		searchResultCheckBox.setValue(isSelected);

		searchResultCheckBox.addValueChangeListener(event -> {
			if (event.getValue()) {
				currentUserRecordSelection.addSelectedRecordId(event.getSource().getId().get());
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

		thumbnail = new Image();
		thumbnail.setAlt(searchResult.getTitle());
		thumbnail.setSrc(searchResult.getImageURL());
		thumbnail.addClassName("metadata-image");

		thumbnailContainer.add(thumbnail);
		add(thumbnailContainer);
	}

	/**
	 * Create metadata component which is part of the result component
	 */
	private void createMetadataComponent() {
		VerticalLayout metadata = new VerticalLayout();
		createMetadataLine(metadata, TITLE_LABEL, searchResult.getTitle());
		createMetadataLine(metadata, AUTHOR_LABEL, searchResult.getAuthor());
		createMetadataLine(metadata, ISSUED_LABEL, searchResult.getIssued());
		createMetadataLine(metadata, PROVIDER_LABEL, searchResult.getProvider());

		RecordTransferValidationCache.ValidationResult validationResult = recordTransferValidationCache.getValidationResult(searchResult.getId());
		if (validationResult == null) {
			createFormatMetadataLine(metadata, searchResult.getId());
		} else {
			createMetadataLine(metadata, FORMAT_LABEL, validationResult.getMimeType());
		}

		createMetadataLine(metadata, LANGUAGE_LABEL, searchResult.getLanguage());
		createMetadataLine(metadata, LICENSE_LABEL, searchResult.getLicense());

		if (validationResult == null) {
			transferPossibilityLine = createLine(TRANSFER_POSSIBILITY_LABEL, TRANSFER_POSSIBILITY_PLACEHOLDER_LABEL);
			metadata.add(transferPossibilityLine);
		} else {
			RecordTransferValidationUtil.TransferPossibility transferPossibility = validationResult.getTransferPossibility();
			transferPossibilityLine = createTransferPossibilityLine(transferPossibility.getMessage(), transferPossibility.isTransferPossible());
			metadata.add(transferPossibilityLine);
			searchResultCheckBox.setEnabled(transferPossibility.isTransferPossible());
		}

		add(metadata);
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

	/**
	 * Create a single line of the format metadata component. Data is loaded asynchronously.
	 *
	 * @param metadata metadata component where the line will be added
	 * @param recordId id of the record
	 */
	private void createFormatMetadataLine(FlexComponent metadata, String recordId) {
		HorizontalLayout tempLine = createLine(FORMAT_LABEL, FORMAT_LOADING_PLACEHOLDER_LABEL);
		UI ui = UI.getCurrent();
		tempLine.addAttachListener(e -> uiPollingManager.registerPollRequest(ui, tempLine, 500));
		tempLine.addDetachListener(e -> uiPollingManager.unregisterPollRequest(ui, tempLine));
		metadata.add(tempLine);

		CompletableFuture.runAsync(() -> {
			JsonObject recordObject = europeanaRestService.retriveRecordFromEuropeanaAndConvertToJsonLd(recordId);
			String mimeType = RecordTransferValidationUtil.getMimeType(recordObject);

			ui.access(() -> {
				HorizontalLayout line = createLine(FORMAT_LABEL, mimeType);
				metadata.replace(tempLine, line);
				updateRecordTransferPossibilityLine(metadata, recordObject, mimeType);
			});
		});
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

	private void updateRecordTransferPossibilityLine(FlexComponent metadata, JsonObject recordObject, String mimeType) {
		RecordTransferValidationUtil.TransferPossibility transferPossibility = RecordTransferValidationUtil.checkIfTransferPossible(recordObject, mimeType);
		String message = transferPossibility.getMessage();
		boolean transferPossible = transferPossibility.isTransferPossible();

		recordTransferValidationCache.addValidationResult(searchResult.getId(), mimeType, transferPossibility);

		HorizontalLayout newTransferPossibilityLine = createTransferPossibilityLine(message, transferPossible);
		metadata.replace(transferPossibilityLine, newTransferPossibilityLine);
		transferPossibilityLine = newTransferPossibilityLine;

		searchResultCheckBox.setEnabled(transferPossible);
		recordEnabled = transferPossible;
	}

	private HorizontalLayout createTransferPossibilityLine(String message, boolean transferPossible) {
		HorizontalLayout line = createLineWithMetadataLabel(TRANSFER_POSSIBILITY_LABEL);
		Label valueLabel = new Label(message);
		if (transferPossible) {
			valueLabel.addClassName("can-transfer-label");
		} else {
			valueLabel.addClassName("cannot-transfer-label");
		}
		line.add(valueLabel);
		line.expand(valueLabel);
		return line;
	}

	public boolean isRecordEnabled() {
		return recordEnabled;
	}
}
