package com.app.e_commerce.listener;

import com.app.e_commerce.services.TrafficService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Session listener to track session end events for traffic analytics
 */
@Component
public class SessionTrackingListener implements HttpSessionListener {

    @Autowired
    private TrafficService trafficService;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Session creation is handled in TrafficService.trackPageView
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // Track session end to calculate session duration and bounce rate
        String sessionId = se.getSession().getId();
        trafficService.trackSessionEnd(sessionId);
    }
}