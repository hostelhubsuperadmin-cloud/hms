package com.hostelhub.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String token = extractCookie(
                request, SecurityConstants.ACCESS_COOKIE
        );

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtService.extractSubject(token);
                UserDetails user =
                        userDetailsService.loadUserByUsername(email);

                if (jwtService.isValid(token, user.getUsername())
                        && user.isEnabled()
                        && user.isAccountNonLocked()) {

                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            } catch (JwtException
                     | IllegalArgumentException
                     | org.springframework.security.core.userdetails
                             .UsernameNotFoundException ex) {
                // Invalid or expired JWT: continue as unauthenticated.
            }
        }

        chain.doFilter(request, response);
    }

    private String extractCookie(
            HttpServletRequest request, String cookieName
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
