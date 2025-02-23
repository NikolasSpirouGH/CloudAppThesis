package com.cloud_ml_app_thesis.config.security;


import com.cloud_ml_app_thesis.service.security.JwtService;
import com.cloud_ml_app_thesis.service.security.UserDetailsServiceImpl;
import com.google.common.net.HttpHeaders;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.var;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException {
        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith(("Bearer "))) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                    if (jwtService.isTokenValid(token)) {
                        var userDetails = userDetailsService.loadUserByUsername(username);

                        //Build the authentication token
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        //Set auth in contect
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }

                }
            }
        } catch (ExpiredJwtException | JwtException e) {
            //TODO
            // You can set custom response status/code here
            // e.g., response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // Then return or continue chain as needed
        }
        try {
            filterChain.doFilter(request, response);
            ;
        } catch (Exception ex) {
            //TODO
        }
    }
}