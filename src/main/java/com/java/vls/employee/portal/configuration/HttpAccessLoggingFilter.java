package com.java.vls.employee.portal.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class HttpAccessLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            log.info(
                    "HTTP request completed: method={}, path={}, status={}, durationMs={}, remoteAddress={}, forwardedHost={}, forwardedProto={}, cfRay={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    clientAddress(request),
                    headerOrBlank(request, "X-Forwarded-Host"),
                    headerOrBlank(request, "X-Forwarded-Proto"),
                    headerOrBlank(request, "CF-Ray")
            );
        }
    }

    private String clientAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String headerOrBlank(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? "" : value;
    }
}
