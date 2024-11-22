package com.pding.paymentservice.service.AdminDashboard;

import com.google.api.services.androidpublisher.model.InAppProduct;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.payload.response.TreeSummary;
import com.pding.paymentservice.payload.response.admin.TreeSummaryGridResult;
import com.pding.paymentservice.payload.response.admin.userTabs.*;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferralCommissionHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferredPdDetails;
import com.pding.paymentservice.payload.response.referralTab.ReferredPDDetailsRecord;
import com.pding.paymentservice.payload.response.referralTab.ReferrerPDDetailsRecord;
import com.pding.paymentservice.paymentclients.google.AppPaymentInitializer;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.service.*;
import com.pding.paymentservice.util.LogSanitizer;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Autowired
    RealTimeLeavesUsageTabService realTimeLeavesUsageTabService;

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    ReferenceTabService referenceTabService;

    @Autowired
    ReferralCommissionService referralCommissionService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    AppPaymentInitializer appPaymentInitializer;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AdminDashboardUserPaymentStatsService.class);

    @Transactional
    public String addTreesFromBackend(String userId, BigDecimal purchasedTrees) throws Exception {
        log.info("Start adding trees by admin for userId {}, trees {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(purchasedTrees));
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
        log.info("Start removing trees by admin for userId {}, trees {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(trees));
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
            log.info("No wallet found for userId {}, can not remove trees", LogSanitizer.sanitizeForLog(userId));
            return "No wallet found for userId " + userId;
        }
    }

    @Transactional
    public String addLeafsFromBackend(String product, String purchaseToken, String email) {
        log.info("Start adding leafs by admin for email {}", LogSanitizer.sanitizeForLog(email));
        try {
            if (paymentService.checkIfTxnIdExists(purchaseToken)) {
                log.error("Transaction Id already present in DB");
                throw new ValidationException("Transaction Id already present in DB");
            }

            ProductPurchase productPurchase = appPaymentInitializer.getProductPurchase(product, purchaseToken);
            InAppProduct inAppProduct = appPaymentInitializer.getInAppProduct(product);

            if (productPurchase.getPurchaseState() != 0) {
                log.error("Cannot add leafs to the user's wallet as the purchase state in play sore is not completed");
                throw new ValidationException("Cannot add leafs to the user's wallet as the purchase state is not completed");
            }

            int purchaseLeaves = 0;
            String productId = product;

            if (productId != null && productId.contains("_")) {
                purchaseLeaves = Integer.parseInt(productId.substring(productId.indexOf("_") + 1));
            } else {
                log.error("Product Id {} is not valid, cannot fetch leafs to add from productId", LogSanitizer.sanitizeForLog(productId));
                throw new ValidationException("Product Id is not valid, cannot fetch leafs to add from productId");
            }

            String userId = otherServicesTablesNativeQueryRepository.getUserIdFromEmail(email);
            String txnId = purchaseToken;
            String paymentMethod = "Google_Play_Store";
            String currency = inAppProduct.getDefaultPrice().get("currency").toString();
            String amountInCents = inAppProduct.getDefaultPrice().get("priceMicros").toString();

            return paymentService.completePaymentToBuyLeafs(
                    userId,
                    new BigDecimal(0),
                    new BigDecimal(purchaseLeaves),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(productPurchase.getPurchaseTimeMillis()), ZoneId.systemDefault()),
                    txnId,
                    TransactionType.PAYMENT_COMPLETED.getDisplayName(),
                    new BigDecimal(amountInCents),
                    paymentMethod,
                    currency,
                    "Added " + purchaseLeaves + " leafs successfully for user.",
                    null
            );
        } catch (Exception e) {
            log.error("Failed to add leafs to the user's wallet with following error: {}", e.getMessage());
            throw new RuntimeException("Internal server error: " + e.getMessage(), e);
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

    public GiftHistoryForPd getGiftHistoryTabForPdDetails(String pdUserId, LocalDate startDate, LocalDate endDate, int page, int size) {
        return giftHistoryTabService.getGiftHistoryTabForPdDetails(pdUserId, startDate, endDate, page, size);
    }

    public WithdrawHistoryForPd getWithdrawHistoryTabForPdDetails(String pdUserId, LocalDate startDate, LocalDate endDate, int sortOrder, int page, int size) {
        return withdrawalService.getWithdrawHistoryTabForPdDetails(pdUserId, startDate, endDate, sortOrder, page, size);
    }


    public TreeSummaryGridResult getTreesSummaryForAllUsers(LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        return treeSummaryTabService.getTreesSummaryForAllUsers(startDate, endDate, searchString, page, size);
    }

    public TreeSummary getTreesSummaryTotals(LocalDate startDate, LocalDate endDate, String searchString) {
        return treeSummaryTabService.getTreesSummaryTotals(startDate, endDate, searchString);
    }

    public TreeSummary getTreesSummaryForPd(String pdUserId) {
        return treeSummaryTabService.getTreesSummaryForPd(pdUserId);
    }

    public RealTimeTreeTransactionHistory getRealTimeTreeUsage(LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        return realTimeTreeUsageTabService.getRealTimeTreeUsage(startDate, endDate, searchString, page, size);
    }

    public TotalTreeUsageSummary getTotalTreeUsageSummary(LocalDate startDate, LocalDate endDate) {
        return realTimeTreeUsageTabService.getTreesSummaryTotals(startDate, endDate);
    }

    public RealTimeLeafTransactionHistory getRealTimeLeafUsage(LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        return realTimeLeavesUsageTabService.getRealTimeLeafsUsage(startDate, endDate, searchString, page, size);
    }


    public TotalLeavesUsageSummary getTotalLeavesUsageSummary(LocalDate startDate, LocalDate endDate) {
        return realTimeLeavesUsageTabService.getLeavesSummaryTotals(startDate, endDate);
    }

    public Page<ReferralCommissionHistory> getReferenceTabDetails(int page, int size, String searchString) throws Exception {
        return referenceTabService.getReferralCommissionHistoryForAdminDashboard(page, size, searchString);
    }

    public Page<ReferredPdDetails> getReferredPdDetails(String referrerPdUserId, int page, int size) {
        return referenceTabService.getReferredPdDetails(referrerPdUserId, page, size);
    }

    public Page<ReferredPDDetailsRecord> listReferredPdDetailsEOL(String referrerPdUserId, LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        return referralCommissionService.listReferredPdDetailsEOL(referrerPdUserId, startDate, endDate, searchString, page, size);
    }

    public Page<ReferrerPDDetailsRecord> listReferrerPdDetails(String referredPdUserId, LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        return referralCommissionService.listReferrerPdDetails(referredPdUserId, startDate, endDate, searchString, page, size);
    }

    public Page<ReferredPDDetailsRecord> listReferredPdDetails(String referrerPdUserId, int page, int size) {
        return referralCommissionService.listReferredPdDetails(referrerPdUserId, page, size);
    }

    public String updateReferralCommissionEntryToCompletedState(String referralCommissionId) throws Exception {
        return referralCommissionService.updateReferralCommissionEntryToCompletedState(referralCommissionId);
    }

    public String refundLeafsFromBackend(String purchaseToken) {
        return paymentService.completeRefundLeafs(purchaseToken);

    }
}
