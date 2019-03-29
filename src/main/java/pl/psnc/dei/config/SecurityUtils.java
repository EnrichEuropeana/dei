package pl.psnc.dei.config;

import com.vaadin.flow.server.ServletHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import org.keycloak.KeycloakPrincipal;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Gets the user name of the currently signed in user. If the principal in the security context is different than
     * KeycloakPrincipal return null.
     *
     * @return the user name of the current user or <code>null</code> if the user
     *         has not signed in
     */
    public static String getUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            return keycloakPrincipal.getName();
        }
        // No authentication.
        return null;
    }


    /**
     * Checks whether a given requests is an internal vaadin framework request.
     * Returns true when the request contains a specific request parameter and its value
     * is one of the known request types.
     *
     * @param request
     *            {@link HttpServletRequest}
     * @return true if this is an internal framework request. False otherwise.
     */
    static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(ServletHelper.RequestType.values()).anyMatch(r -> r.getIdentifier().equals(parameterValue));
    }

    /**
     * Checks whether the logged in user has access to the target page. When the user is not logged in it returns false.
     * Otherwise it checks for Spring Security annotation in the target page class and compares them against the authorities
     * retrieved from the authentication object. When no annotation is found in the target page class access is granted.
     *
     * @param navigationTarget target page to be checked for access
     * @return true when logged in user has access to the target page, false otherwise.
     */
    public static boolean isAccessGranted(Class<?> navigationTarget) {
        Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isUserLoggedIn(userAuthentication)) {
            return false;
        }

        Secured securedResources = AnnotationUtils.findAnnotation(navigationTarget, Secured.class);
        if (securedResources == null) {
            return true;
        }

        List<String> roles = Arrays.asList(securedResources.value());
        return userAuthentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roles::contains);
    }

    /**
     * Checks whether a user stored in the security context is logged in. This is true when the authentication token
     * is not anonymous.
     *
     * @return true when the current user is logged in.
     */
    public static boolean isUserLoggedIn() {
        return isUserLoggedIn(SecurityContextHolder.getContext().getAuthentication());
    }

    private static boolean isUserLoggedIn(Authentication userAuthentication) {
        return userAuthentication != null && !(userAuthentication instanceof AnonymousAuthenticationToken);
    }

}
