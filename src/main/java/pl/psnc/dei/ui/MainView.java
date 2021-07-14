package pl.psnc.dei.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import pl.psnc.dei.config.SecurityUtils;
import pl.psnc.dei.ui.components.ListeningVerticalLayout;
import pl.psnc.dei.ui.pages.AccessDeniedPage;
import pl.psnc.dei.ui.pages.BatchPage;
import pl.psnc.dei.ui.pages.ImportPage;
import pl.psnc.dei.ui.pages.LogoutPage;
import pl.psnc.dei.ui.pages.SearchPage;

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

        Tab homeTab = prepareTab("Home", MainView.class, VaadinIcon.HOME);
        Tab searchTab = prepareTab("Search", SearchPage.class, VaadinIcon.SEARCH);
        Tab importTab = prepareTab("Import", ImportPage.class, VaadinIcon.ANGLE_RIGHT);
        Tab batchTab = prepareTab("Batch", BatchPage.class, VaadinIcon.COPY_O);
        Tab logoutTab = prepareTab("Logout", LogoutPage.class, VaadinIcon.POWER_OFF);

        menuTabs.add(homeTab, searchTab, importTab, batchTab, logoutTab);

        HorizontalLayout header = new HorizontalLayout(title, menuTabs);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setAlignSelf(Alignment.START, title);
        add(header);
    }

    private Tab prepareTab(String linkDesc, Class<? extends Component> page, VaadinIcon icon) {
        RouterLink link = new RouterLink(linkDesc, page);
        link.getStyle().set("font-size", "1em");
        // Only show as active for the exact URL, but not for sub paths
        link.setHighlightCondition(HighlightConditions.sameLocation());
        ListeningVerticalLayout layout = new ListeningVerticalLayout(new Icon(icon), link);
        layout.addClickListener(e -> layout.getUI().ifPresent(ui -> ui.navigate(page)));
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        return new Tab(layout);
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
            case "pl.psnc.dei.ui.pages.BatchPage":
                menuTabs.setSelectedIndex(3);
                break;
            case "pl.psnc.dei.ui.pages.LogoutPage":
                menuTabs.setSelectedIndex(4);
                break;
            default:
                menuTabs.setSelectedIndex(0);
        }
    }
}
