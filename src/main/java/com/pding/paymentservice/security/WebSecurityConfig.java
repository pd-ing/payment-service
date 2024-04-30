package com.pding.paymentservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.OPTIONS).permitAll()
                                //.requestMatchers("/api/payment/charge").authenticated()
                                .requestMatchers("/api/payment/test").authenticated()
                                .requestMatchers("/api/payment/wallet").authenticated()
                                .requestMatchers("/api/payment/walletHistory").authenticated()
                                .requestMatchers("/api/payment/buyVideo").authenticated()
                                .requestMatchers("/api/payment/videoPurchaseHistory").authenticated()
                                .requestMatchers("/api/payment/videoPurchaseHistory").authenticated()
                                .requestMatchers("/api/payment/videoPurchaseHistoryOfUser").authenticated()
                                //.requestMatchers("/api/payment/treesEarned").authenticated()
                                .requestMatchers("/api/payment/isVideoPurchased").authenticated()
                                .requestMatchers("/api/payment/donationHistoryForUser").authenticated()
                                .requestMatchers("/api/payment/donationHistoryForPd").authenticated()
                                .requestMatchers("/api/payment/donationHistoryWithVideoStatsForPd").authenticated()
                                .requestMatchers("/api/payment/donate").authenticated()
                                .requestMatchers("/api/payment/topDonorsList").permitAll()
                                .requestMatchers("/api/payment/webhook").permitAll()
                                .requestMatchers("/api/payment/videoEarningAndSales").permitAll()
                                .requestMatchers("/api/payment/videoPurchaseReplacement").permitAll()
                                .requestMatchers("/api/payment/getAllPdWhoseVideosArePurchasedByUser").authenticated()
                                .requestMatchers("/api/payment/buyCall").authenticated()
                                .requestMatchers("/api/payment/callHistoryForPd").authenticated()
                                .requestMatchers("/api/payment/callHistoryForUser").authenticated()
                                .requestMatchers("/api/payment/topCallersForPd").authenticated()
                                .requestMatchers("/api/payment/topCallers").authenticated()
                                .requestMatchers("/api/payment/startWithDraw").authenticated()
                                .requestMatchers("/api/payment/completeWithDraw").authenticated()
                                .requestMatchers("/api/payment/failWithDraw").authenticated()
                                .requestMatchers("/api/payment/withDrawTransactions").authenticated()
                                .requestMatchers("/api/payment/admin/pendingWithDrawTransactions").authenticated()
                                .requestMatchers("/api/payment/admin/allWithDrawTransactions").authenticated()
                                .requestMatchers("/api/payment/admin/balanceTrees").permitAll()
                                .requestMatchers("/api/payment/admin/addTrees").permitAll()
                                .requestMatchers("/api/payment/admin/removeTrees").permitAll()
                                .requestMatchers("/api/payment/admin/statusTab").permitAll()
                                .requestMatchers("/api/payment/admin/statusTabForPd").permitAll()
                                .requestMatchers("/api/payment/admin/viewingHistoryTab").permitAll()
                                .requestMatchers("/api/payment/admin/paymentHistoryTab").permitAll()
                                .requestMatchers("/api/payment/admin/paymentHistoryAllUsersTab").permitAll()
                                .requestMatchers("/api/payment/admin/paymentHistoryAllUsersSearchByEmail").permitAll()
                                .requestMatchers("/api/payment/admin/giftHistoryTab").permitAll()
                                .requestMatchers("/api/payment/admin/viewingHistoryTabSearchVideo").permitAll()
                                .requestMatchers("/api/payment/topEarners").permitAll()
                                .requestMatchers("/api/payment/topFans").authenticated()
                                .requestMatchers("/api/payment/getPurchaserOfVideo").authenticated()
                                .requestMatchers("/api/payment/v2/buyVideo").authenticated()
                                .requestMatchers("/api/payment/v2/donate").authenticated()
                                //.requestMatchers("/api/payment/v2/charge").authenticated()
                                .requestMatchers("/api/payment/startPaymentToBuyTrees").authenticated()
                                .requestMatchers("/api/payment/clearPendingAndStalePayments/**").permitAll()
                                .requestMatchers("/api/payment/paymentsFailedInitiallyButSucceededLater").permitAll()
                                .requestMatchers("/api/payment/treeSpentHistory").authenticated()
                                .requestMatchers("/api/payment/updateRewardSettings").authenticated()
                                .requestMatchers("/api/payment/getRewardSettings").authenticated()
                                .requestMatchers("/api/payment/videoSalesHistoryOfPd").authenticated()
                                .requestMatchers("/api/payment/dailyTreeRevenueOfPd").permitAll()
                                .requestMatchers("/api/payment/searchVideoSalesHistoryOfPd").authenticated()
                                .requestMatchers("/api/payment/admin/treeSummariesAllPd").permitAll()
                                .requestMatchers("/api/payment/admin/treeSummariesTotals").permitAll()
                                .requestMatchers("/api/payment/admin/realTimeTreeUsageHistory").permitAll()
                                .requestMatchers("/api/payment/admin/realTimeTreeUsageTotals").permitAll()
                );


        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}