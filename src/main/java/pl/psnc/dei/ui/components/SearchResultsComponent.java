package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.schema.search.SearchResults;

@StyleSheet("frontend://styles/styles.css")
public class SearchResultsComponent extends VerticalLayout {
    private static final int DEFAULT_PAGE_SIZE = 12;

    private static final String AUTHOR_LABEL = "Author:";

    private static final String TITLE_LABEL = "Title:";

    private static final String ISSUED_LABEL = "Issued:";

    private static final String PROVIDER_LABEL = "Provider institution:";

    private static final String FORMAT_LABEL = "Format:";

    private static final String LANGUAGE_LABEL = "Language:";

    private static final String LICENSE_LABEL = "License:";

    private transient SearchResults searchResults;

    public SearchResultsComponent(SearchResults results) {
        addClassName("search-results-component");
        this.searchResults = results;
        setPadding(false);

        updateComponent();
    }

    private void updateComponent() {
        removeAll();
        setVisible(searchResults != null
                && searchResults.getResults() != null
                && !searchResults.getResults().isEmpty());

        if (isVisible()) {
            // add results count
            Label resultsCount = new Label(prepareResultsText());
            add(resultsCount);

            // add result list
            VerticalLayout resultsList = new VerticalLayout();
            resultsList.setPadding(false);
            searchResults.getResults().forEach(searchResult -> {
                resultsList.add(createResultComponent(searchResult));
            });
            add(resultsList);
        }
    }

    public void setSearchResults(SearchResults results) {
        this.searchResults = results;
        updateComponent();
    }

    private Component createResultComponent(SearchResult searchResult) {
        HorizontalLayout resultComponent = new HorizontalLayout();
        resultComponent.addClassName("search-result-element");
        resultComponent.setSizeFull();
        resultComponent.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        Checkbox checkbox = new Checkbox();
        checkbox.addClassName("search-result-checkbox");
        resultComponent.add(checkbox);
        Image image = createImage(searchResult);
        resultComponent.add(image);
        resultComponent.add(createMetadataComponent(searchResult));
        return resultComponent;
    }

    private Component createMetadataComponent(SearchResult searchResult) {
        VerticalLayout metadata = new VerticalLayout();
        createMetadataLine(metadata, TITLE_LABEL, searchResult.getTitle());
        createMetadataLine(metadata, AUTHOR_LABEL, searchResult.getAuthor());
        createMetadataLine(metadata, ISSUED_LABEL, searchResult.getIssued());
        createMetadataLine(metadata, PROVIDER_LABEL, searchResult.getProvider());
        createMetadataLine(metadata, FORMAT_LABEL, searchResult.getFormat());
        createMetadataLine(metadata, LANGUAGE_LABEL, searchResult.getLanguage());
        createMetadataLine(metadata, LICENSE_LABEL, searchResult.getLicense());
        return metadata;
    }

    private void createMetadataLine(FlexComponent metadata, String label, String value) {
        if (value != null && !value.isEmpty()) {
            HorizontalLayout line = new HorizontalLayout();
            line.addClassName("metadata-line");
            line.setPadding(false);
            line.setVerticalComponentAlignment(Alignment.START);
            Label name = new Label(label);
            name.addClassName("metadata-label");
            line.add(name);
            Label valueLabel = new Label(value);
            line.add(valueLabel);
            line.expand(valueLabel);
            metadata.add(line);
        }
    }

    private Image createImage(SearchResult result) {
        Image image = new Image();
        image.setAlt(result.getTitle());
        image.setSrc(result.getImageURL());
        image.addClassName("metadata-image");
        return image;
    }

    private String prepareResultsText() {
        return String.valueOf((searchResults.getResultsCollected() >= DEFAULT_PAGE_SIZE ? searchResults.getResultsCollected() - DEFAULT_PAGE_SIZE : 0) + 1)
                + " - "
                + searchResults.getResultsCollected()
                + " of "
                + searchResults.getTotalResults();
    }
}
