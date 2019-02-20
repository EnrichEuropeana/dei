package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

class PageNavigationComponent extends HorizontalLayout {
    private int totalPages;

    private int currentPage;

    private SearchResultsComponent searchResultsComponent;

    /**
     * Create page navigation component by associating it to the corresponding search results component. The component
     * is also initiated with items per page and total pages calculated from the given total items.
     * @param searchResultsComponent search results component associated with this component
     * @param itemsPerPage number of items per page needed to calculate total number of pages
     * @param totalItems total number of results needed to calculate total number of pages
     */
    PageNavigationComponent(SearchResultsComponent searchResultsComponent, int itemsPerPage, int totalItems) {
        calculatePages(itemsPerPage, totalItems);
        this.searchResultsComponent = searchResultsComponent;

        addClassName("page-navigation-bar");
        setPadding(false);
        setDefaultVerticalComponentAlignment(Alignment.CENTER);

        updateElements();
    }

    /**
     * Resets the number of pages when new search was performed.
     * @param itemsPerPage items per page
     * @param totalItems number of results
     */
    void resetPages(int itemsPerPage, int totalItems) {
        calculatePages(itemsPerPage, totalItems);
        updateElements();
    }

    private void calculatePages(double itemsPerPage, double totalItems) {
        if (itemsPerPage <= 0) {
            itemsPerPage = SearchResultsComponent.DEFAULT_PAGE_SIZE;
        }
        this.totalPages = (int) Math.ceil(totalItems / itemsPerPage);
        this.currentPage = 1;
    }

    private void updateElements() {
        removeAll();

        // <
        Button button = createButton(VaadinIcon.ANGLE_LEFT, currentPage > 1);
        button.addClickListener(buttonClickEvent -> {
            if (currentPage > 1) {
                setCurrentPage(currentPage - 1);
            }
        });
        add(button);

        if (totalPages <= 5) {
            for (int i = 1; i <= totalPages; i++) {
                add(createButton(i));
            }
        } else {
            // 1
            add(createButton(1));

            // ... if necessary
            createDotsButton(true);

            // c-2, c-1, c, c+1, c+2
            addMiddleButtons();

            // ... if necessary
            createDotsButton(false);

            // last page
            add(createButton(totalPages));
        }

        // >
        button = createButton(VaadinIcon.ANGLE_RIGHT, currentPage < totalPages);
        button.addClickListener(buttonClickEvent -> {
            if (currentPage < totalPages) {
                setCurrentPage(currentPage + 1);
            }
        });
        add(button);
    }

    private void createDotsButton(boolean first) {
        if (currentPage >= 5 && first) {
            add(createButton(VaadinIcon.ELLIPSIS_DOTS_H, false));
        }
        if (currentPage <= totalPages - 4 && !first) {
            add(createButton(VaadinIcon.ELLIPSIS_DOTS_H, false));
        }
    }

    private void addMiddleButtons() {
        for (int i = currentPage - 2; i <= currentPage + 2; i++) {
            if (i < 2 || i >= totalPages) {
                continue;
            }
            add(createButton(i));
        }
    }

    private Button createButton(VaadinIcon icon, boolean enabled) {
        Button button = new Button(new Icon(icon));
        button.addClassName("navigation-button");
        button.setEnabled(enabled);
        return button;
    }

    private Button createButton(int i) {
        Button button = new Button(String.valueOf(i));
        button.addClassName("navigation-button");
        if (i == currentPage) {
            button.addClassName("navigation-button-current");
            button.setEnabled(false);
        }
        button.addClickListener(buttonClickEvent -> {
           if (currentPage != i) {
               setCurrentPage(i);
           }
        });
        return button;
    }

    private void setCurrentPage(int page) {
        searchResultsComponent.goToPage(currentPage, page);
        this.currentPage = page;
        updateElements();
    }
}
