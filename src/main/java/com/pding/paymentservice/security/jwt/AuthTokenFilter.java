package com.pding.paymentservice.security.jwt;

import com.pding.paymentservice.exception.ExpiredJwtTokenException;
import com.pding.paymentservice.exception.InvalidJwtTokenException;
import com.pding.paymentservice.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        List<String> errors = new ArrayList<>();
        try {
            String jwt = parseJwt(request);
            if (jwt == null) {
                throw new JwtAuthenticationException("JWT token not provided by user.");
            }

            if (jwtUtils.validateJwtToken(jwt)) {
                Claims claims = jwtUtils.getAllClaimsFromToken(jwt);
                Integer userIdInteger = (Integer) claims.get("userid");
                Long userId = userIdInteger != null ? userIdInteger.longValue() : null;
                String username = (String) claims.get("username"); // Use "username" as the claim name
                List<GrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);

                // Create an Authentication object with custom claims
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        new UserDetailsImpl(userId, username, authorities), // Customize UserDetailsImpl constructor as needed
                        null,
                        authorities
                );

                // Set the authentication in the SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (InvalidJwtTokenException | ExpiredJwtTokenException e) {
            logger.error("JWT validation error: {}", e.getMessage());
            errors.add(e.getMessage());
            request.setAttribute("jwtErrors", errors);
        } catch (JwtAuthenticationException e) {
            logger.error("JWT Token error: {}", e.getMessage());
            errors.add(e.getMessage());
            request.setAttribute("jwtErrors", errors);
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
            errors.add(e.getMessage());
            request.setAttribute("jwtErrors", errors);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }

        return null;
    }

    private List<GrantedAuthority> extractAuthoritiesFromClaims(Claims claims) {
        // Assuming "roles" claim contains an ArrayList of LinkedHashMap objects
        ArrayList<LinkedHashMap<String, String>> roleClaims = (ArrayList<LinkedHashMap<String, String>>) claims.get("roles");

        return roleClaims.stream()
                .flatMap(roleClaim -> roleClaim.values().stream())
                .filter(roleName -> "true".equals(roleName)) // Filter by "true" values
                .map(roleName -> new SimpleGrantedAuthority(roleName))
                .collect(Collectors.toList());
    }
}
