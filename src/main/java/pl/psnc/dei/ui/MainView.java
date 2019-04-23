package pl.psnc.dei.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import pl.psnc.dei.config.SecurityUtils;
import pl.psnc.dei.ui.pages.*;

@Route(value = "")
@PageTitle("Data Exchange Infrastructure application")
public class MainView extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

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

        RouterLink importLink = new RouterLink("Import", ImportPage.class);
        importLink.getStyle().set("font-size", "1em");
        // Only show as active for the exact URL, but not for sub paths
        importLink.setHighlightCondition(HighlightConditions.sameLocation());
        VerticalLayout importLayout = new VerticalLayout(new Icon(VaadinIcon.ANGLE_RIGHT), importLink);
        importLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Tab importTab = new Tab(importLayout);

        RouterLink logout = new RouterLink("Logout", LogoutPage.class);
        logout.getStyle().set("font-size", "1em");
        // Only show as active for the exact URL, but not for sub paths
        logout.setHighlightCondition(HighlightConditions.sameLocation());
        VerticalLayout logoutLayout = new VerticalLayout(VaadinIcon.POWER_OFF.create(), logout);
        logoutLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Tab logoutTab = new Tab(logoutLayout);

        menuTabs.add(homeTab, searchTab, importTab, importsHistoryTab(), logoutTab);

        HorizontalLayout header = new HorizontalLayout(title, menuTabs);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setAlignSelf(Alignment.START, title);
        add(header);
    }

    private Tab importsHistoryTab() {
        RouterLink importsHistoryLink = new RouterLink("History", ImportsHistory.class);
        importsHistoryLink.getStyle().set("font-size", "1em");
        // Only show as active for the exact URL, but not for sub paths
        importsHistoryLink.setHighlightCondition(HighlightConditions.sameLocation());
        VerticalLayout importHistoryLayout = new VerticalLayout(new Icon(VaadinIcon.CALENDAR_O), importsHistoryLink);
        importHistoryLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        return new Tab(importHistoryLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!SecurityUtils.isAccessGranted(event.getNavigationTarget())) {
            if (!SecurityUtils.isUserLoggedIn()) {
                event.getUI().getPage().reload(); // should redirect to login page
            } else if (event.getNavigationTarget() != AccessDeniedPage.class) {
                event.rerouteTo(AccessDeniedPage.class);
            }
        }
    }
}
