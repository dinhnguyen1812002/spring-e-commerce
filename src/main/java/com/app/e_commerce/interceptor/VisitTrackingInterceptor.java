package com.app.e_commerce.interceptor;

import com.app.e_commerce.services.TrafficService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class VisitTrackingInterceptor implements HandlerInterceptor {
    @Autowired
    private TrafficService trafficService;

    // Paths to exclude from tracking (admin paths, static resources, etc.)
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/css/", "/js/", "/images/", "/fonts/", "/favicon.ico", "/error"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip tracking for excluded paths
        String path = request.getRequestURI();
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }

        // Track visit for homepage
        if ("GET".equalsIgnoreCase(request.getMethod()) && "/".equals(path)) {
            trafficService.trackVisit();
        }

        // Track page view for all pages
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            HttpSession session = request.getSession(true);
            trafficService.trackPageView(request, session);
        }

        return true;  // Continue the request
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // We don't track session end here because sessions typically end due to timeout,
        // not at the end of a request. Session end tracking should be handled by a
        // HttpSessionListener implementation.
    }
}
