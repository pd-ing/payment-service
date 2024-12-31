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

    @Autowired
    public AuthTokenFilter authenticationJwtTokenFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.OPTIONS).permitAll()
                                //.requestMatchers("/api/payment/charge").authenticated()
                                .requestMatchers("/api/payment/appConfig/leafDonationConfig").authenticated()
                                .requestMatchers("/api/payment/app/listProducts").authenticated()
                                .requestMatchers("/api/payment/app/listProducts/v2").authenticated()
                                .requestMatchers("/api/payment/test").authenticated()
                                .requestMatchers("/api/payment/wallet").authenticated()
                                .requestMatchers("/api/payment/walletHistory").authenticated()
                                .requestMatchers("/api/payment/leafsWalletHistory").authenticated()
                                .requestMatchers("/api/payment/buyVideo").authenticated()
                                .requestMatchers("/api/payment/videoPurchaseHistory").authenticated()
                                .requestMatchers("/api/payment/videoPurchaseHistory").authenticated()
                                .requestMatchers("/api/payment/expiredVideoPurchases").authenticated()
                                .requestMatchers("/api/payment/videoPurchaseHistoryOfUser").authenticated()
                                //.requestMatchers("/api/payment/treesEarned").authenticated()
                                .requestMatchers("/api/payment/isVideoPurchased").authenticated()
                                .requestMatchers("/api/payment/v2/isVideoPurchased").authenticated()
                                //.requestMatchers("/api/payment/paidUnpaidFollowerList").authenticated()
                                .requestMatchers("/api/payment/paidUnpaidFollowerCount").authenticated()
                                .requestMatchers("/api/payment/donationHistoryForUser").authenticated()
                                .requestMatchers("/api/payment/donationHistoryForPd").authenticated()
                                .requestMatchers("/api/payment/donationHistoryWithVideoStatsForPd").authenticated()
                                .requestMatchers("/api/payment/donate").authenticated()
                                .requestMatchers("/api/payment/topDonorsList").permitAll()
                                .requestMatchers("/api/payment/topDonorsList/v2").permitAll()
                                .requestMatchers("/api/payment/topDonorsListDownload").permitAll()
                                .requestMatchers("/api/payment/topDonorsListDownloadPreparing").permitAll()
                                .requestMatchers("/api/payment/webhook").permitAll()
                                .requestMatchers("/api/payment/ggPlayStoreWebhook").permitAll()
                                .requestMatchers("/api/payment/appStoreWebhook").permitAll()
                                .requestMatchers("/api/payment/videoEarningAndSales").permitAll()
//                                .requestMatchers("/api/payment/videoPurchaseReplacement").permitAll()
                                .requestMatchers("/api/payment/getAllPdWhoseVideosArePurchasedByUser").authenticated()
                                .requestMatchers("/api/payment/buyCallOrMessage").authenticated()
                                .requestMatchers("/api/payment/addBuyCallOrMessageEntryInRealTimeDb").authenticated()
                                .requestMatchers("/api/payment/callHistoryForPd").authenticated()
                                .requestMatchers("/api/payment/callHistoryForUser").authenticated()
                                .requestMatchers("/api/payment/topCallersForPd").authenticated()
                                .requestMatchers("/api/payment/topCallers").authenticated()
                                .requestMatchers("/api/payment/startWithDraw").authenticated()
                                .requestMatchers("/api/payment/completeWithDraw").authenticated()
                                .requestMatchers("/api/payment/failWithDraw").authenticated()
                                .requestMatchers("/api/payment/withDrawTransactions").authenticated()
                                .requestMatchers("/api/payment/admin/completeReferralCommission").authenticated()
                                .requestMatchers("/api/payment/getDetailsOfAllTheReferredPd").authenticated()
                                .requestMatchers("/api/payment/getReferralCommissionDetailsWithFilters").authenticated()
//                                .requestMatchers("/api/payment/tokens/register").authenticated()
//                                .requestMatchers("/api/payment/tokens/delete").authenticated()
//                                .requestMatchers("/api/payment/tokens/sendNotification").authenticated()
//                                .requestMatchers("/api/payment/tokens/sendGenericNotification").permitAll()
                                .requestMatchers("/api/payment/admin/pendingWithDrawTransactions").authenticated()
                                .requestMatchers("/api/payment/admin/allWithDrawTransactions").authenticated()
                                .requestMatchers("/api/payment/admin/balanceTrees").authenticated()
                                .requestMatchers("/api/payment/admin/listReferredPdsEOL").authenticated()
                                .requestMatchers("/api/payment/admin/listReferrerPds").authenticated()
                                .requestMatchers("/api/payment/admin/listReferredPds").authenticated()
                                .requestMatchers("/api/payment/admin/addTrees").authenticated()
                                .requestMatchers("/api/payment/admin/removeTrees").authenticated()
                                .requestMatchers("/api/payment/admin/addLeafs").authenticated()
                                .requestMatchers("/api/payment/admin/refundLeafs").authenticated()
                                .requestMatchers("/api/payment/admin/statusTab").authenticated()
                                .requestMatchers("/api/payment/admin/statusTabForPd").authenticated()
                                .requestMatchers("/api/payment/admin/viewingHistoryTab").authenticated()
                                .requestMatchers("/api/payment/admin/viewingHistoryTabForPd").authenticated()
                                .requestMatchers("/api/payment/admin/paymentHistoryTab").authenticated()
                                .requestMatchers("/api/payment/admin/paymentHistoryAllUsersTab").authenticated()
                                .requestMatchers("/api/payment/admin/paymentHistoryAllUsersSearchByEmail").authenticated()
                                .requestMatchers("/api/payment/admin/giftHistoryTab").authenticated()
                                .requestMatchers("/api/payment/admin/withdrawalHistoryTabForPd").authenticated()
                                .requestMatchers("/api/payment/admin/giftHistoryTabForPd").authenticated()
                                .requestMatchers("/api/payment/admin/viewingHistoryTabSearchVideo").authenticated()
                                .requestMatchers("/api/payment/admin/referenceTabDetails").authenticated()
                                .requestMatchers("/api/payment/admin/modalForReferenceTab").authenticated()
                                .requestMatchers("/api/payment/topEarners").permitAll()
                                .requestMatchers("/api/payment/topFans").authenticated()
                                .requestMatchers("/api/payment/getPurchaserOfVideo").authenticated()
                                .requestMatchers("/api/payment/v2/buyVideo").authenticated()
                                .requestMatchers("/api/payment/v3/buyVideo").authenticated()
                                .requestMatchers("/api/payment/v2/donate").authenticated()
                                .requestMatchers("/api/payment/donateLeafs").authenticated()
                                //.requestMatchers("/api/payment/v2/charge").authenticated()
                                .requestMatchers("/api/payment/startPaymentToBuyTrees").authenticated()
                                .requestMatchers("/api/payment/clearPendingAndStalePayments/**").authenticated()
                                .requestMatchers("/api/payment/paymentsFailedInitiallyButSucceededLater").authenticated()
                                .requestMatchers("/api/payment/buyLeafs").authenticated()
                                .requestMatchers("/api/payment/buyLeafsIOS").authenticated()
                                .requestMatchers("/api/payment/buyLeafsIOSSandBox").authenticated()
                                .requestMatchers("/api/payment/getIosTransactionDetails").authenticated()
                                .requestMatchers("/api/payment/getIosAppStoreConnectToken").authenticated()
                                .requestMatchers("/api/payment/treeSpentHistory").authenticated()
                                .requestMatchers("/api/payment/updateRewardSettings").authenticated()
                                .requestMatchers("/api/payment/getRewardSettings").authenticated()
                                .requestMatchers("/api/payment/videoSalesHistoryOfPd").authenticated()
                                .requestMatchers("/api/payment/dailyTreeRevenueOfPd").permitAll()
                                .requestMatchers("/api/payment/salesHistoryDownload").permitAll()
                                .requestMatchers("/api/payment/salesHistoryDownloadPreparing").permitAll()
                                .requestMatchers("/api/payment/searchVideoSalesHistoryOfPd").authenticated()
                                .requestMatchers("/api/payment/admin/treeSummariesAllPd").permitAll()
                                .requestMatchers("/api/payment/admin/treeSummariesTotals").permitAll()
                                .requestMatchers("/api/payment/admin/treeSummariesPd").permitAll()
                                .requestMatchers("/api/payment/admin/realTimeTreeUsageHistory").permitAll()
                                .requestMatchers("/api/payment/admin/realTimeTreeUsageTotals").permitAll()
                                .requestMatchers("/api/payment/admin/realTimeLeavesUsageTotals").authenticated()
                                .requestMatchers("/api/payment/admin/realTimeLeavesUsageHistory").authenticated()
                                .requestMatchers("/api/payment/internal/mediaTrading").permitAll()
                                .requestMatchers("/api/payment/mediaTrading/buy").authenticated()
                                .requestMatchers("/api/payment/mediaTrading").authenticated()
                                .requestMatchers("/api/payment/mediaTrading/cancel").authenticated()
                                .requestMatchers("/api/payment/videoPurchaseTimeRemaining").permitAll()
                                .requestMatchers("/api/payment/buyImagePost").authenticated()
                                .requestMatchers("/api/payment/isImagePostPurchased").authenticated()
                                .requestMatchers("/api/payment/getPurchasedImagePosts").authenticated()
                                .requestMatchers("/api/payment/allPdWhosePostsArePurchasedByUser").authenticated()
                                .requestMatchers("/api/payment/statistic/leafsEarningFromCallHistory").authenticated()
                                .requestMatchers("/api/payment/statistic/leafsEarningFromGiftHistory").authenticated()
                                .requestMatchers("/api/payment/statistic/leafPaymentHistory").authenticated()
                                .requestMatchers("/api/payment/statistic/pdSummary").authenticated()
                                .requestMatchers("/api/payment/statistic/leafPaymentHistorySummary").authenticated()
                                .requestMatchers("/api/payment/statistic/videoSaleHistorySummary").authenticated()
                                .requestMatchers("/api/payment/statistic/videoSaleHistory").authenticated()
                                .requestMatchers("/api/payment/statistic/gross-revenue-graph").authenticated()
                                .requestMatchers("/api/payment/statistic/gross-revenue-graph-by-date-range").authenticated()
                                .requestMatchers("/api/payment/paypal-webhook").permitAll()
                                .requestMatchers("/actuator/health/*").permitAll()
                                .requestMatchers("/api/payment/paypal/webhook").permitAll()
                                .requestMatchers("/api/payment/paypal/createOrder").authenticated()
                                .requestMatchers("/api/payment/paypal/captureOrder").authenticated()

                );


        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
