package com.app.e_commerce.interceptor;

import com.app.e_commerce.services.TrafficService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

@Component
public class VisitTrackingInterceptor implements HandlerInterceptor {
    @Autowired
    private TrafficService trafficService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        trafficService.trackVisit();

        return true;  // Continue the request
    }
}
