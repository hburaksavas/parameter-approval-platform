package com.example.parameterapproval.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private final String userIdHeader;
    private final String userNameHeader;
    private final String rolesHeader;

    public HeaderAuthenticationFilter(
            @Value("${app.security.user-id-header}") String userIdHeader,
            @Value("${app.security.user-name-header}") String userNameHeader,
            @Value("${app.security.roles-header}") String rolesHeader) {
        this.userIdHeader = userIdHeader;
        this.userNameHeader = userNameHeader;
        this.rolesHeader = rolesHeader;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userId = request.getHeader(userIdHeader);
        if (userId != null && !userId.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String displayName = request.getHeader(userNameHeader);
            String roles = request.getHeader(rolesHeader);
            List<SimpleGrantedAuthority> authorities = roles == null ? List.of() : Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            HeaderUser principal = new HeaderUser(userId, displayName == null ? userId : displayName);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, authorities));
        }
        filterChain.doFilter(request, response);
    }
}

