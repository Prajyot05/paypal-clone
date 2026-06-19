package com.paypal.common.util;

import java.io.IOException;
import java.util.List;

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
public class JWTrequestFilter extends OncePerRequestFilter{

    private JWTUtil jwtUtil;
    public JWTrequestFilter(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                final String authorizationHeader = request.getHeader("Authorization");
                String username = null;
                String jwt = null;

                if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                    jwt = authorizationHeader.substring(7);
                    if(jwt == null || jwt.isBlank()){
                        filterChain.doFilter(request, response);
                        return;
                    }
                    try {
                        username = jwtUtil.extractUsername(jwt);
                        String role = jwtUtil.extractRole(jwt);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role)));

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                    } catch (Exception e) {
                        System.err.println("JWT Validation Error: " + e.getMessage());
                    }
                }
                
                filterChain.doFilter(request, response);
    }

}
