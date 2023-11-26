package com.pding.paymentservice.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.ExpiredJwtTokenException;
import com.pding.paymentservice.exception.InvalidJwtTokenException;
import com.pding.paymentservice.exception.JwtAuthenticationException;
import com.pding.paymentservice.security.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    PdLogger pdLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idToken = parseJwt(request);

        // for CORS error
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            SecurityContextHolder.getContext().setAuthentication(null);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken, true);
            String userId = firebaseToken.getUid();
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);

            LoggedInUserRecord loggedInUserRecord = LoggedInUserRecord.fromUserRecord(userRecord);
            loggedInUserRecord.setIdToken(idToken);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            loggedInUserRecord,
                            null,
                            null
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (FirebaseAuthException e) {
            pdLogger.logException(PdLogger.EVENT.FIREBASE_AUTH, e);
            request.setAttribute("jwtErrors", e.getErrorCode() + " - " + e.getMessage());
            throw new SecurityException("Error in Authentication: " + e.getErrorCode().name());
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.SERVICE_AUTH, e);
            request.setAttribute("jwtErrors", e.getMessage());
            throw new SecurityException("Error in Authentication: " + e.getMessage());
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

}
