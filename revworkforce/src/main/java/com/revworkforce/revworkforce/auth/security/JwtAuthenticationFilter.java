package com.revworkforce.revworkforce.auth.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 🔥 Skip authentication for auth endpoints
    	if (request.getServletPath().startsWith("/auth")) {
    	    filterChain.doFilter(request, response);
    	    return;
    	}

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);

                if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                    String role = jwtUtil.extractRole(token);
                    System.out.println("USERNAME FROM TOKEN: " + username);
                    System.out.println("ROLE FROM TOKEN: " + role);
                    

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singleton(
                                            new SimpleGrantedAuthority("ROLE_" + role)
                                    )
                            );

                    // 🔥 DEBUG PRINTS HERE
                    System.out.println("USERNAME: " + username);
                    System.out.println("ROLE FROM TOKEN: " + role);
                    System.out.println("AUTHORITIES SET: " + authentication.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // If token is invalid, just continue without authentication
            }
        }

        filterChain.doFilter(request, response);
    }
    
}