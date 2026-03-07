package net.blackhacker.ares;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

// Removed @Component to prevent auto-registration. We will register it manually in SecurityConfig.
@Profile("!test")
public class MaintenanceModeFilter extends OncePerRequestFilter {

    final private Collection<String> permittedRoles = List.of("ROLE_TEST_USER", "ROLE_ADMIN");
    final private Collection<String> permittedPaths = List.of("/api/login", "/api/register", "/api/features", "/api/admin");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if(permittedPaths.stream().anyMatch(path::startsWith)) {
            //Always let these paths go through
            filterChain.doFilter(request, response);
            return;
        }

        if (AresFeatures.MAINTENANCE_MODE.isActive()) {
            if (currentUserIsNotPermitted()) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write("Site is currently under maintenance.");
                return;
            }
        }

        /*
        if (!AresFeatures.PUBLIC_LAUNCH.isActive()) {
            if (currentUserIsNotPermitted()) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write("Site is currently unavailable (Pre-Launch).");
                return;
            }
        }
        */

        filterChain.doFilter(request, response);
    }

    private boolean currentUserIsNotPermitted() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return true;
        }

        return auth.getAuthorities().stream()
                .noneMatch(grantedAuthority ->
                        permittedRoles.contains(grantedAuthority.getAuthority())
                );
    }
}
