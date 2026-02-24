package com.app.e_commerce.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;

import java.io.IOException;

public class CustomRememberMeAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof CookieTheftException || (exception == null && isCookieTheft(request))) {
            response.sendRedirect("/auth/login?error=cookieTheft");
        } else {
            response.sendRedirect("/auth/login?error=loginError");
        }
    }

    private boolean isCookieTheft(HttpServletRequest request) {
        // If we are called from loginFail with null exception, we might need to check if it was theft.
        // But since PersistentTokenBasedRememberMeServices only calls loginFail for non-theft cases too,
        // we might want to store a flag in request if theft was detected.
        return Boolean.TRUE.equals(request.getAttribute("COOKIE_THEFT_DETECTED"));
    }
}
