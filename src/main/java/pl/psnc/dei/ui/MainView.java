package pl.psnc.dei.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import pl.psnc.dei.ui.pages.SearchPage;

@Route(value = "")
@PageTitle("Data Exchange Infrastructure application")
public class MainView extends VerticalLayout implements RouterLayout {

    public MainView() {
        H2 title = new H2("Data Exchange Infrastructure");
        title.getStyle().set("font-size", "1em");
        title.getStyle().set("width", "105px");

        Tabs menuTabs = new Tabs();
        RouterLink home = new RouterLink("Home", MainView.class);
        home.getStyle().set("font-size", "1em");
        // Only show as active for the exact URL, but not for sub paths
        home.setHighlightCondition(HighlightConditions.sameLocation());
        VerticalLayout homeLayout = new VerticalLayout(new Icon(VaadinIcon.HOME), home);
        homeLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Tab homeTab = new Tab(homeLayout);

        RouterLink search = new RouterLink("Search", SearchPage.class);
        search.getStyle().set("font-size", "1em");
        // Only show as active for the exact URL, but not for sub paths
        search.setHighlightCondition(HighlightConditions.sameLocation());
        VerticalLayout searchLayout = new VerticalLayout(new Icon(VaadinIcon.SEARCH), search);
        searchLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Tab searchTab = new Tab(searchLayout);

        menuTabs.add(homeTab, searchTab);

        HorizontalLayout header = new HorizontalLayout(title, menuTabs);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setAlignSelf(Alignment.START, title);
        add(header);
    }
}
