package com.pding.paymentservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.pding.paymentservice.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    List<String> permitAllEndpoints = Arrays.asList(
            "/api/payment/webhook",
            "/api/payment/paypal-webhook",
            "/api/payment/ggPlayStoreWebhook",
            "/api/payment/appStoreWebhook",
            "/api/payment/videoEarningAndSales",
            "/api/payment/topEarners",
            "/api/payment/clearPendingAndStalePayments",
            "/api/payment/clearPendingPayment",
            "/api/payment/dailyTreeRevenueOfPd",
            "/api/payment/admin/treeSummariesAllPd",
            "/api/payment/admin/treeSummariesTotals",
            "/api/payment/dailyTreeRevenueOfPd",
            "/api/payment/topDonorsList",
            "/api/payment/admin/realTimeTreeUsageHistory",
            "/api/payment/admin/realTimeTreeUsageTotals",
            "/api/payment/admin/referenceTabDetails",
            "/api/payment/admin/modalForReferenceTab",
            "/api/payment/admin/listReferredPdsEOL",
            "/api/payment/admin/listReferrerPds",
            "/api/payment/admin/listReferredPds",
            "/api/payment/tokens/sendGenericNotification",
            "/api/payment/internal/mediaTrading",
            "/api/payment/videoPurchaseTimeRemaining",
            "/actuator",
            "/api/internal"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String idToken = parseJwt(request);
        String serverToken = parseServerToken(request);
        return permitAllEndpoints.stream().anyMatch(request.getRequestURI()::startsWith) && idToken == null && serverToken == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idToken = parseJwt(request);
        String serverToken = parseServerToken(request);

        // If this is a Firebase token request, check if it was invalidated globally
        String invalidKey = "um:invalid:" + idToken;
        String val = redisTemplate.opsForValue().get(invalidKey);
        if (val != null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("errorCode", "ERR_SESSION_REPLACED");
            result.put("message", "Session invalidated. This session has been replaced by another device. Please login again.");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return;
        }


        // for CORS error
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            SecurityContextHolder.getContext().setAuthentication(null);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String userId;
            String email = null;
            Boolean emailVerified = null;
            Boolean isAdmin = false;
            Boolean isBanned = false;
            Boolean isSalesTeam = false;
            if (serverToken != null && !serverToken.isEmpty()) {
                userId = getUidFromServerToken(serverToken);
            } else if (idToken != null && !idToken.isEmpty()) {
                FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken, false);
                userId = firebaseToken.getUid();
                email = firebaseToken.getEmail();
                emailVerified = firebaseToken.isEmailVerified();
                isAdmin = Boolean.TRUE.equals(firebaseToken.getClaims().get("admin"));
                isBanned = Boolean.TRUE.equals(firebaseToken.getClaims().get("isBanned"));
                isSalesTeam = Boolean.TRUE.equals(firebaseToken.getClaims().get("isSalesTeam"));

                if(isBanned) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("message", "Account is banned");
                    response.getWriter().write(objectMapper.writeValueAsString(result));
                    return;
                }
            } else {
                userId = null;
            }

            PdingSecurityHolder holder = PdingSecurityHolder.builder()
                    .uid(userId)
                    .idToken(idToken)
                    .email(email)
                    .emailVerified(emailVerified)
                    .request(request)
                    .build();

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if(isAdmin) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            if(isSalesTeam) {
                authorities.add(new SimpleGrantedAuthority("ROLE_SALES_TEAM"));
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            holder,
                            null,
                        authorities
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (FirebaseAuthException e) {
            //pdLogger.logException(PdLogger.Priority.p0, e);
            request.setAttribute("jwtErrors", e.getErrorCode() + " - " + e.getMessage());
            throw new SecurityException("Error in Authentication: " + e.getErrorCode().name());
        } catch (Exception e) {
            //pdLogger.logException(PdLogger.Priority.p0, e);
            request.setAttribute("jwtErrors", e.getMessage());
            throw new SecurityException("Error in Authentication: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getUidFromServerToken(String serverToken) {
        return jwtUtils.getUserIdFromToken(serverToken);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private String parseServerToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("BearerServer ")) {
            return headerAuth.substring(13);
        }
        return null;
    }

}
