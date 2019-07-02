package pl.psnc.dei.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import pl.psnc.dei.config.SecurityUtils;
import pl.psnc.dei.ui.components.ListeningVerticalLayout;
import pl.psnc.dei.ui.pages.AccessDeniedPage;
import pl.psnc.dei.ui.pages.ImportPage;
import pl.psnc.dei.ui.pages.LogoutPage;
import pl.psnc.dei.ui.pages.SearchPage;

//@Push
@Route(value = "")
@PageTitle("Data Exchange Infrastructure application")
@BodySize(height = "100%", width = "100%")
public class MainView extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

	private Tabs menuTabs;

	public MainView() {
		H2 title = new H2("Data Exchange Infrastructure");
		title.getStyle().set("font-size", "1em");
		title.getStyle().set("width", "105px");

		menuTabs = new Tabs();
		RouterLink home = new RouterLink("Home", MainView.class);
		home.getStyle().set("font-size", "1em");
		// Only show as active for the exact URL, but not for sub paths
		home.setHighlightCondition(HighlightConditions.sameLocation());
		ListeningVerticalLayout homeLayout = new ListeningVerticalLayout(new Icon(VaadinIcon.HOME), home);
		homeLayout.addClickListener(e -> homeLayout.getUI().ifPresent(ui -> ui.navigate(MainView.class)));
		homeLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		Tab homeTab = new Tab(homeLayout);

		RouterLink search = new RouterLink("Search", SearchPage.class);
		search.getStyle().set("font-size", "1em");
		// Only show as active for the exact URL, but not for sub paths
		search.setHighlightCondition(HighlightConditions.sameLocation());
		ListeningVerticalLayout searchLayout = new ListeningVerticalLayout(new Icon(VaadinIcon.SEARCH), search);
		searchLayout.addClickListener(e -> searchLayout.getUI().ifPresent(ui -> ui.navigate(SearchPage.class)));
		searchLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		Tab searchTab = new Tab(searchLayout);

		RouterLink importLink = new RouterLink("Import", ImportPage.class);
		importLink.getStyle().set("font-size", "1em");
		// Only show as active for the exact URL, but not for sub paths
		importLink.setHighlightCondition(HighlightConditions.sameLocation());
		ListeningVerticalLayout importLayout = new ListeningVerticalLayout(new Icon(VaadinIcon.ANGLE_RIGHT), importLink);
		importLayout.addClickListener(e -> importLayout.getUI().ifPresent(ui -> ui.navigate(ImportPage.class)));
		importLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		Tab importTab = new Tab(importLayout);

		RouterLink logout = new RouterLink("Logout", LogoutPage.class);
		logout.getStyle().set("font-size", "1em");
		// Only show as active for the exact URL, but not for sub paths
		logout.setHighlightCondition(HighlightConditions.sameLocation());
		ListeningVerticalLayout logoutLayout = new ListeningVerticalLayout(VaadinIcon.POWER_OFF.create(), logout);
		logoutLayout.addClickListener(e -> logoutLayout.getUI().ifPresent(ui -> ui.navigate(LogoutPage.class)));
		logoutLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		Tab logoutTab = new Tab(logoutLayout);

		menuTabs.add(homeTab, searchTab, importTab, logoutTab);

		HorizontalLayout header = new HorizontalLayout(title, menuTabs);
		header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		header.setAlignSelf(Alignment.START, title);
		add(header);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (!SecurityUtils.isAccessGranted(event.getNavigationTarget())) {
			if (!SecurityUtils.isUserLoggedIn()) {
				event.getUI().getPage().reload(); // should redirect to login page
			} else if (event.getNavigationTarget() != AccessDeniedPage.class) {
				event.rerouteTo(AccessDeniedPage.class);
			}
		} else if (event.getNavigationTarget().getName().equals("pl.psnc.dei.ui.MainView")) {
			UI.getCurrent().getPage().executeJavaScript("history.pushState(history.state,'',' ');");
		}

		switch (event.getNavigationTarget().getName()) {
			case "pl.psnc.dei.ui.pages.SearchPage":
				menuTabs.setSelectedIndex(1);
				break;
			case "pl.psnc.dei.ui.pages.ImportPage":
				menuTabs.setSelectedIndex(2);
				break;
			case "pl.psnc.dei.ui.pages.LogoutPage":
				menuTabs.setSelectedIndex(3);
				break;

			default:
				menuTabs.setSelectedIndex(0);
		}
	}
}
