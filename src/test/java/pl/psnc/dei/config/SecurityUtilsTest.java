package pl.psnc.dei.config;

import com.vaadin.flow.server.ServletHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.psnc.dei.ui.pages.SearchPage;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class SecurityUtilsTest {

    private static final String TEST_USERNAME = "test_user";

    @Test
    public void getUsernameForLoggedIn() {
        // prepare
        mockAuthentication(false);

        // when
        String username = SecurityUtils.getUsername();

        // then
        Assert.assertNotNull(username);
        Assert.assertEquals(TEST_USERNAME, username);
    }


    @Test
    public void getUsernameForAnonymous() {
        // prepare
        mockAuthentication(true);

        // when
        String username = SecurityUtils.getUsername();

        // then
        Assert.assertNull(username);
    }

    private void mockAuthentication(boolean anonymous) {
        Authentication authentication;

        if (anonymous) {
            authentication = Mockito.mock(AnonymousAuthenticationToken.class);
            Mockito.when(authentication.getPrincipal()).thenReturn(TEST_USERNAME);
        } else {
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            grantedAuthorities.add((GrantedAuthority) () -> Role.OPERATOR);
            KeycloakAccount account = Mockito.mock(KeycloakAccount.class);
            KeycloakPrincipal principal = Mockito.mock(KeycloakPrincipal.class);
            Mockito.when(account.getPrincipal()).thenReturn(principal);
            authentication = new KeycloakAuthenticationToken(account, false, grantedAuthorities);
            Mockito.when(authentication.getPrincipal()).thenReturn(principal);
            Mockito.when(principal.getName()).thenReturn(TEST_USERNAME);
        }

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void isFrameworkInternalRequest() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        // UIDL
        Mockito.when(request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER)).thenReturn(ServletHelper.RequestType.UIDL.getIdentifier());
        Assert.assertTrue(SecurityUtils.isFrameworkInternalRequest(request));

        // HEARTBEAT
        Mockito.when(request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER)).thenReturn(ServletHelper.RequestType.HEARTBEAT.getIdentifier());
        Assert.assertTrue(SecurityUtils.isFrameworkInternalRequest(request));

        // PUSH
        Mockito.when(request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER)).thenReturn(ServletHelper.RequestType.PUSH.getIdentifier());
        Assert.assertTrue(SecurityUtils.isFrameworkInternalRequest(request));

        // Other
        Mockito.when(request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER)).thenReturn("value");
        Assert.assertFalse(SecurityUtils.isFrameworkInternalRequest(request));
    }

    @Test
    public void isAccessGrantedWhenUserNotLoggedIn() {
        mockAuthentication(true);

        Assert.assertFalse(SecurityUtils.isAccessGranted(SearchPage.class));
    }

    @Test
    public void isAccessGrantedWhenUserLoggedIn() {
        mockAuthentication(false);

        Assert.assertTrue(SecurityUtils.isAccessGranted(SearchPage.class));
    }

    @Test
    public void isAnonymousUserLoggedIn() {
        mockAuthentication(true);

        Assert.assertFalse(SecurityUtils.isUserLoggedIn());
    }

    @Test
    public void isNormalUserLoggedIn() {
        mockAuthentication(false);

        Assert.assertTrue(SecurityUtils.isUserLoggedIn());
    }

}