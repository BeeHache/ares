package net.blackhacker.ares;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
@Profile("!test")
public class MaintenanceModeFilter extends OncePerRequestFilter {

    final private Collection<String> permittedRoles = List.of("ROLE_TEST_USER", "ROLE_ADMIN");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Allow access to the Togglz console so you can turn it back off!
        if (request.getRequestURI().startsWith("/features")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (AresFeatures.MAINTENANCE_MODE.isActive() && !currentUserIsPermitted()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("Site is currently under maintenance. Please try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean currentUserIsPermitted() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        permittedRoles.contains(grantedAuthority.getAuthority())
                );
    }
}
