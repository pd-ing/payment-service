package com.pding.paymentservice.security;

import com.google.firebase.auth.*;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.security.jwt.JwtUtils;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    PdLogger pdLogger;

    @Autowired
    private JwtUtils jwtUtils;

    List<String> permitAllEndpoints = Arrays.asList(
            "/api/payment/topDonorsList",
            "/api/payment/webhook",
            "/api/payment/videoEarningAndSales",
            "/api/payment/topEarners",
            "/api/payment/admin/balanceTrees",
            "/api/payment/admin/addTrees",
            "/api/payment/admin/removeTrees",
            "/api/payment/admin/statusTab",
            "/api/payment/clearPendingPayment",
            "/api/payment/paymentsFailedInitiallyButSucceededLater",
            "/api/payment/clearPendingPayment",
            "/api/payment/admin/viewingHistoryTab"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idToken = parseJwt(request);
        String serverToken = parseServerToken(request);

        if (permitAllEndpoints.stream().anyMatch(request.getRequestURI()::startsWith) && idToken == null) {
            // No need of authentication for this one.
            SecurityContextHolder.getContext().setAuthentication(null);
            filterChain.doFilter(request, response);
            return;
        }

        setSentryScope(request, idToken, serverToken);

        // for CORS error
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            SecurityContextHolder.getContext().setAuthentication(null);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String userId;

            if (serverToken != null && !serverToken.isEmpty()) {
                userId = getUidFromServerToken(serverToken);
            } else if (idToken != null && !idToken.isEmpty()) {
                FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken, true);
                userId = firebaseToken.getUid();
            } else {
                userId = null;
            }

            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
            setSentryUserScope(userRecord);

            LoggedInUserRecord loggedInUserRecord = LoggedInUserRecord.fromUserRecord(userRecord, request);
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

    private void setSentryScope(HttpServletRequest request, String idToken, String serverToken) {
        Sentry.configureScope(scope -> {
            scope.setExtra("httpApiEndpoint", request.getRequestURI());
            scope.setExtra("httpMethod", request.getMethod());
            scope.setExtra("httpRemoteAddr", request.getRemoteAddr());
            scope.setExtra("httpUserAgent", request.getHeader("User-Agent"));
            scope.setExtra("httpQueryString", request.getQueryString());
            scope.setExtra("httpRequestParameters", extractRequestParameters(request));
            scope.setExtra("isIdTokenPresent", idToken == null || idToken.isEmpty() ? "false" : "true");
            scope.setExtra("isFromInternalServer", serverToken == null || serverToken.isEmpty() ? "false" : "true");
            scope.setExtra("platform", request.getHeader("PDing-Platform"));
            scope.setExtra("clientVersion", request.getHeader("PDing-ClientVersion"));
        });
    }

    private void setSentryUserScope(UserRecord record) {
        User user = new User();
        user.setEmail(record.getEmail());
        user.setId(record.getUid());

        Map<String, String> data = new HashMap<>();
        data.put("providers", Arrays.stream(record.getProviderData())
                .map(UserInfo::getProviderId)
                .collect(Collectors.joining(",")));
        user.setData(data);

        Sentry.configureScope(scope -> {
            scope.setExtra("isIdTokenValid", "true");
            scope.setUser(user);
        });
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }

    private String parseServerToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("BearerServer ")) {
            return headerAuth.substring(13, headerAuth.length());
        }
        return null;
    }

    private static String extractRequestParameters(HttpServletRequest request) {
        StringBuilder formattedParameters = new StringBuilder();

        Map<String, String[]> parameterMap = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            for (String paramValue : paramValues) {
                formattedParameters.append(paramName)
                        .append("=")
                        .append(paramValue)
                        .append(", ");
            }
        }

        // Remove the trailing comma and space
        if (formattedParameters.length() > 0) {
            formattedParameters.setLength(formattedParameters.length() - 2);
        }

        return formattedParameters.toString();
    }

}
