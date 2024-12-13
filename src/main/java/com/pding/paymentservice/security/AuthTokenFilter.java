package com.pding.paymentservice.security;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    List<String> permitAllEndpoints = Arrays.asList(
            "/api/payment/webhook",
            "/api/payment/ggPlayStoreWebhook",
            "/api/payment/appStoreWebhook",
            "/api/payment/videoEarningAndSales",
            "/api/payment/topEarners",
//            "/api/payment/admin/balanceTrees",
//            "/api/payment/admin/addTrees",
//            "/api/payment/admin/removeTrees",
//            "/api/payment/admin/statusTab",
//            "/api/payment/admin/statusTabForPd",
            "/api/payment/clearPendingAndStalePayments",
            "/api/payment/clearPendingPayment",
//            "/api/payment/admin/viewingHistoryTab",
//            "/api/payment/admin/giftHistoryTab",
//            "/api/payment/admin/giftHistoryTabForPd",
//            "/api/payment/admin/withdrawalHistoryTabForPd",
//            "/api/payment/paymentsFailedInitiallyButSucceededLater",
//            "/api/payment/admin/viewingHistoryTab",
//            "/api/payment/admin/viewingHistoryTabForPd",
//            "/api/payment/admin/viewingHistoryTabSearchVideo",
//            "/api/payment/admin/paymentHistoryTab",
//            "/api/payment/admin/paymentHistoryAllUsersTab",
//            "/api/payment/admin/paymentHistoryAllUsersSearchByEmail",
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
            "/actuator"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String idToken = parseJwt(request);
        return permitAllEndpoints.stream().anyMatch(request.getRequestURI()::startsWith) && idToken == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idToken = parseJwt(request);
        String serverToken = parseServerToken(request);

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
                FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken, false);
                userId = firebaseToken.getUid();
            } else {
                userId = null;
            }

            PdingSecurityHolder holder = PdingSecurityHolder.builder()
                    .token(idToken)
                    .uid(userId)
                    .request(request)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            holder,
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

}
