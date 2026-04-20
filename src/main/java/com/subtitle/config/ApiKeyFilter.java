package com.subtitle.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.api-key}")
    private String apiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().contains("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerKey = request.getHeader("X-API-KEY");

        if (apiKey != null && apiKey.equals(headerKey)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(401);
            response.getWriter().write("Unauthorized");
        }
    }
}
