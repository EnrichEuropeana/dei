package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.psnc.dei.config.SecurityUtils;
import pl.psnc.dei.ui.MainView;

@Route(value = "logout", layout = MainView.class)
public class LogoutPage extends HorizontalLayout implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (SecurityUtils.isUserLoggedIn()
                && beforeEnterEvent.getTrigger() != NavigationTrigger.PAGE_LOAD) {
            try {
                UI.getCurrent().getSession().close();
                UI.getCurrent().getSession().getSession().invalidate();
                VaadinServletRequest.getCurrent().getHttpServletRequest().logout();
                SecurityContextHolder.clearContext();
                beforeEnterEvent.forwardTo(MainView.class);
            } catch (Exception e) {
                // redirect to error page
            }
        } else {
            beforeEnterEvent.forwardTo(MainView.class);
        }
    }
}
