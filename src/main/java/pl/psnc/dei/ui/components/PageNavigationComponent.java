package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.HashMap;
import java.util.Map;

@StyleSheet("frontend://styles/styles.css")
class PageNavigationComponent extends HorizontalLayout {

	private static final Map<Integer, Integer> THRESHOLDS;

	static{
		THRESHOLDS = new HashMap<>();
		THRESHOLDS.put(10,50);
		THRESHOLDS.put(20,40);
		THRESHOLDS.put(50,30);
		THRESHOLDS.put(100,20);
	}

    private int totalPages;
    private int itemsPerPage;
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
        this.itemsPerPage = itemsPerPage;
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
        this.itemsPerPage = itemsPerPage;
        calculatePages(itemsPerPage, totalItems);
        updateElements();
    }

    /**
     * Calculate number of pages based on total items and items per page
     * @param itemsPerPage number of items per page
     * @param totalItems number of all items
     */
    private void calculatePages(double itemsPerPage, double totalItems) {
        if (itemsPerPage <= 0) {
            itemsPerPage = SearchResultsComponent.DEFAULT_PAGE_SIZE;
        }
        this.totalPages = (int) Math.ceil(totalItems / itemsPerPage);
        this.currentPage = 1;
    }

    /**
     * Update buttons with available pages.
     */
    private void updateElements() {
        removeAll();

        // < - button for going back one page
        Button button = createButton(VaadinIcon.ANGLE_LEFT, currentPage > 1);
        button.addClickListener(buttonClickEvent -> {
            if (currentPage > 1) {
                setCurrentPage(currentPage - 1);
            }
        });
        add(button);

        if (totalPages <= 5) {
            // if there are 1 to 5 pages all buttons are displayed
            for (int i = 1; i <= totalPages; i++) {
                add(createButton(i));
            }
        } else {
            // 1 - first page
            add(createButton(1));

            // ... if necessary
            createDotsButton(true);

            // c-2, c-1, c, c+1, c+2 - buttons for current page (c), two previous pages (c-1,c-2) and two next pages (c+1,c+2)
            addMiddleButtons();

            // ... if necessary
            createDotsButton(false);

            // last page

            add(createLastPageButton(currentPage, totalPages));
        }

        // > - button for moving to next page
        button = createButton(VaadinIcon.ANGLE_RIGHT, currentPage < totalPages);
        button.addClickListener(buttonClickEvent -> {
            if (currentPage < totalPages) {
                setCurrentPage(currentPage + 1);
            }
        });
        add(button);
    }

    /**
     * blocking last pages if too much pages, performance restriction
     *
     * @return
     */
    private Button createLastPageButton(int currentPage, int totalPages) {
        final Button lastPageButton = createButton(totalPages);
        boolean enableLastPage = shouldEnableLastPage(currentPage, totalPages);
        lastPageButton.setEnabled(enableLastPage);
        if (!enableLastPage) {
            lastPageButton.addClassName("button-disabled");
        }
        return lastPageButton;
    }

    private boolean shouldEnableLastPage(int currentPage, int totalPages) {
		return totalPages - currentPage < THRESHOLDS.get(itemsPerPage);
    }

    /**
     * Creates button with dots. It is always disabled.
     * @param first when true button after first page is considered otherwise button before the last page
     */
    private void createDotsButton(boolean first) {
        if (currentPage >= 5 && first) {
            add(createButton(VaadinIcon.ELLIPSIS_DOTS_H, false));
        }
        if (currentPage <= totalPages - 4 && !first) {
            add(createButton(VaadinIcon.ELLIPSIS_DOTS_H, false));
        }
    }

    /**
     * Creates buttons for pages c-2,c-1,c,c+1,c+2 where c is the current page
     */
    private void addMiddleButtons() {
        for (int i = currentPage - 2; i <= currentPage + 2; i++) {
            if (i < 2 || i >= totalPages) {
                continue;
            }
            add(createButton(i));
        }
    }

    /**
     * Creates a button with an icon and enables / disables it.
     * @param icon icon to be shown on the button
     * @param enabled enable state
     * @return created button
     */
    private Button createButton(VaadinIcon icon, boolean enabled) {
        Button button = new Button(new Icon(icon));
        button.addClassName("navigation-button");
        button.setEnabled(enabled);
        button.setDisableOnClick(true);
        return button;
    }

    /**
     * Create button with page number
     * @param i page number
     * @return created button
     */
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
        button.setDisableOnClick(true);
        return button;
    }

    /**
     * Navigate to the specified page
     * @param page page number to navigate to
     */
    private void setCurrentPage(int page) {
        searchResultsComponent.goToPage(currentPage, page);
        this.currentPage = page;
        updateElements();
    }
}
