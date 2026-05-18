package com.java.vls.employee.portal.jwt;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // Your JWT utility class

    private final UserDetailsService userDetailsService; // Ensure this is your CustomUserDetailsService

    public  JwtRequestFilter(JwtUtil jwtUtil,UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "/".equals(path)
                || "/index.html".equals(path)
                || path.endsWith(".html")
                || path.startsWith("/auth/")
                || path.startsWith("/api/")
                || path.startsWith("/api-docs/")
                || path.startsWith("/.well-known/")
                || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (JwtException | IllegalArgumentException ex) {
                log.warn("Rejected JWT while extracting username: path={}, remoteAddress={}, reason={}",
                        request.getRequestURI(), request.getRemoteAddr(), ex.getClass().getSimpleName());
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or unsupported JWT token");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            try {
                if (!jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    log.warn("Rejected invalid JWT: username={}, path={}, remoteAddress={}",
                            username, request.getRequestURI(), request.getRemoteAddr());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                    return;
                }
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("JWT authenticated: username={}, path={}", username, request.getRequestURI());
            } catch (JwtException | IllegalArgumentException ex) {
                log.warn("Rejected JWT during validation: username={}, path={}, remoteAddress={}, reason={}",
                        username, request.getRequestURI(), request.getRemoteAddr(), ex.getClass().getSimpleName());
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or unsupported JWT token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
