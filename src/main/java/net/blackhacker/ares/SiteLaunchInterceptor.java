package net.blackhacker.ares;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class SiteLaunchInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        // Allow access to login, admin pages, and the Togglz console
        if (request.getRequestURI().startsWith("/api/login") ||
                request.getRequestURI().startsWith("/features")) {
            return true;
        }

        if (AresFeatures.PUBLIC_LAUNCH.isActive()) {
            return true; // Site is live, allow request
        } else {
            // Site is not live, show maintenance page
            response.sendRedirect("/coming-soon.html");
            return false; // Stop the request
        }
    }
}
