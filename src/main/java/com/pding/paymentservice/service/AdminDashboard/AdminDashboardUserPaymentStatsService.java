package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.response.TreeSummary;
import com.pding.paymentservice.payload.response.admin.TreeSummaryGridResult;
import com.pding.paymentservice.payload.response.admin.userTabs.*;
import com.pding.paymentservice.service.LedgerService;
import com.pding.paymentservice.service.WalletHistoryService;
import com.pding.paymentservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminDashboardUserPaymentStatsService {

    @Autowired
    WalletService walletService;

    @Autowired
    WalletHistoryService walletHistoryService;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    StatusTabService statusTabService;

    @Autowired
    ViewingHistoryTabService viewingHistoryTabService;

    @Autowired
    PaymentHistoryTabService paymentHistoryTabService;

    @Autowired
    GiftHistoryTabService giftHistoryTabService;

    @Autowired
    TreeSummaryTabService treeSummaryTabService;

    @Autowired
    RealTimeTreeUsageTabService realTimeTreeUsageTabService;


    @Transactional
    public String addTreesFromBackend(String userId, BigDecimal purchasedTrees) throws Exception {
        try {
            Optional<Wallet> walletOptional = walletService.fetchWalletByUserId(userId);
            if (walletOptional.isPresent()) {
                Wallet wallet = walletService.updateWalletForUser(userId, purchasedTrees, new BigDecimal(0), LocalDateTime.now());

                UUID uuid = UUID.randomUUID();
                walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, purchasedTrees, new BigDecimal(0), LocalDateTime.now(), uuid.toString(), TransactionType.ADD_TREES_FROM_BACKEND.getDisplayName(),
                        new BigDecimal(0), "", "", "Added trees for the user using Admin dashbaord", "");

                ledgerService.saveToLedger(wallet.getId(), purchasedTrees, new BigDecimal(0), TransactionType.ADD_TREES_FROM_BACKEND, userId);

                return "Successfully added trees for the user";
            } else {
                return "No wallet found for userId " + userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Payment Details update failed with following error : " + e.getMessage());
        }
    }

    @Transactional
    public String removeTreesFromBackend(String userId, BigDecimal trees) {
        Optional<Wallet> walletOptional = walletService.fetchWalletByUserId(userId);
        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            walletService.deductTreesFromWallet(userId, trees);
            UUID uuid = UUID.randomUUID();
            walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, trees, new BigDecimal(0), LocalDateTime.now(), uuid.toString(), TransactionType.REMOVE_TREES_FROM_BACKEND.getDisplayName(),
                    new BigDecimal(0), "", "", "Removed trees for the user using Admin dashbaord", "");
            ledgerService.saveToLedger(wallet.getId(), trees, new BigDecimal(0), TransactionType.REMOVE_TREES_FROM_BACKEND, userId);
            return "Successfully removed trees for the user";
        } else {
            return "No wallet found for userId " + userId;
        }
    }


    public Status getStatusTabDetails(String userId) {
        return statusTabService.getStatusTabDetails(userId);
    }

    public StatusForPd getStatusTabForPdDetails(String userId) {
        return statusTabService.getStatusTabDetailsForPd(userId);
    }

    public ViewingHistory getViewingHistory(String userId, int page, int size) {
        return viewingHistoryTabService.getViewingHistory(userId, page, size);
    }

    public ViewingHistoryForPd getViewingHistoryForPd(String userId, String searchString, int page, int size) {
        return viewingHistoryTabService.getViewingHistoryForPd(userId, searchString, page, size);
    }

    public ViewingHistory searchVideo(String userId, String videoTitle, int page, int size) {
        return viewingHistoryTabService.searchVideo(userId, videoTitle, page, size);
    }

    public PaymentHistory getPaymentHistory(String userId, int page, int size) {
        return paymentHistoryTabService.getPaymentHistory(userId, page, size);
    }

    public PaymentHistory getPaymentHistoryForAllUsers(LocalDate startDate, LocalDate endDate, int sortOrder, int page, int size) {
        return paymentHistoryTabService.getPaymentHistoryAllUsers(startDate, endDate, sortOrder, page, size);
    }

    public PaymentHistory searchPaymentHistoryByEmail(String searchString, int page, int size) {
        return paymentHistoryTabService.searchByEmail(searchString, page, size);
    }

    public GiftHistory getGiftHistoryTabDetails(String userId, int page, int size) {
        return giftHistoryTabService.getGiftHistoryTabDetails(userId, page, size);
    }

    public TreeSummaryGridResult getTreesSummaryForAllUsers (LocalDate startDate, LocalDate endDate, String searchString, int page, int size){
        return treeSummaryTabService.getTreesSummaryForAllUsers(startDate, endDate, searchString, page, size);
    }

    public TreeSummary getTreesSummaryTotals (){
        return treeSummaryTabService.getTreesSummaryTotals();
    }

    public RealTimeTreeTransactionHistory getRealTimeTreeUsage (LocalDate startDate, LocalDate endDate, String searchString, int page, int size){
        return realTimeTreeUsageTabService.getRealTimeTreeUsage(startDate, endDate, searchString, page, size);
    }

    public TotalTreeUsageSummary getTotalTreeUsageSummary (LocalDate startDate, LocalDate endDate){
        return realTimeTreeUsageTabService.getTreesSummaryTotals(startDate, endDate);
    }

}
