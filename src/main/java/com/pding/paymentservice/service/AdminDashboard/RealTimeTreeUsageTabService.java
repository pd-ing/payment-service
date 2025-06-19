package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.TreeSummary;
import com.pding.paymentservice.payload.response.UserObject;
import com.pding.paymentservice.payload.response.admin.TreeSummaryGridResult;
import com.pding.paymentservice.payload.response.admin.userTabs.RealTimeTreeTransactionHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.TotalTreeUsageSummary;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.TransactionHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.RealTimeTreeUsageTabRepository;
import com.pding.paymentservice.repository.admin.TreeSummaryTabRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RealTimeTreeUsageTabService {
    @Autowired
    RealTimeTreeUsageTabRepository realTimeTreeUsageTabRepository;

    @Autowired
    TokenSigner tokenSigner;


    public RealTimeTreeTransactionHistory getRealTimeTreeUsage(LocalDate startDate, LocalDate endDate, String transactionType, String searchString, int page, int size) {
        RealTimeTreeTransactionHistory result = new RealTimeTreeTransactionHistory();
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_update_date").descending());
        if (searchString.isEmpty()) {
            searchString = null;
        }
        Page<Object[]> transaction = realTimeTreeUsageTabRepository.getRealTimeTreeUsage(startDate, endDate, transactionType, searchString, pageable);
        List<TransactionHistoryForAdminDashboard> transactionList = createTreeUsageSummaryList(transaction.getContent());
        result.setTransactionHistoryForAdminDashboards(new PageImpl<>(transactionList, pageable, transaction.getTotalElements()));
        return result;
    }

    public TotalTreeUsageSummary getTreesSummaryTotals(LocalDate startDate, LocalDate endDate) {
        TotalTreeUsageSummary treeSummary = new TotalTreeUsageSummary();
        BigDecimal totalTreesTransacted = new BigDecimal(0.00);
        CompletableFuture<BigDecimal> videoTransactionFuture = CompletableFuture.supplyAsync(
            () -> realTimeTreeUsageTabRepository.getTotalTreesTransactedForVideos(startDate, endDate));

        CompletableFuture<BigDecimal> donatedFuture = CompletableFuture.supplyAsync(
            () -> realTimeTreeUsageTabRepository.getTotalTreesDonated(startDate, endDate));

        CompletableFuture<BigDecimal> exposureTicketsFuture = CompletableFuture.supplyAsync(
            () -> realTimeTreeUsageTabRepository.getTotalTreesTransactedForExposureTickets(startDate, endDate));

        CompletableFuture<BigDecimal> videoPackagesFuture = CompletableFuture.supplyAsync(
            () -> realTimeTreeUsageTabRepository.getTotalTreesTransactedForVideoPackages(startDate, endDate));

        CompletableFuture.allOf(videoTransactionFuture, donatedFuture, exposureTicketsFuture, videoPackagesFuture).join();

        BigDecimal totalTreesVideoTransaction = videoTransactionFuture.join();
        BigDecimal totalTreesDonated = donatedFuture.join();
        BigDecimal totalTreesTransactedForExposureTickets = exposureTicketsFuture.join();
        BigDecimal totalTreesTransactedForVideoPackages = videoPackagesFuture.join();



        totalTreesTransacted = totalTreesVideoTransaction.add(totalTreesDonated).add(totalTreesTransactedForExposureTickets).add(totalTreesTransactedForVideoPackages);
        treeSummary.setTotalTreesTransacted(totalTreesTransacted);
        treeSummary.setTotalTreesVideoTransaction(totalTreesVideoTransaction);
        treeSummary.setTotalTreesDonated(totalTreesDonated);
        treeSummary.setTotalTreesTransactedForExposureTickets(totalTreesTransactedForExposureTickets);
        treeSummary.setTotalTreesTransactedForVideoPackages(totalTreesTransactedForVideoPackages);
        return treeSummary;
    }


    private List<TransactionHistoryForAdminDashboard> createTreeUsageSummaryList(List<Object[]> transactionPage) {
        List<TransactionHistoryForAdminDashboard> treeUsageList = new ArrayList<>();
        for (Object innerObject : transactionPage) {
            Object[] realTimeTreeTransactionHistory = (Object[]) innerObject;
            TransactionHistoryForAdminDashboard tranObj = new TransactionHistoryForAdminDashboard();
            tranObj.setUserEmail(realTimeTreeTransactionHistory[0].toString());
            tranObj.setUserId(realTimeTreeTransactionHistory[1].toString());
            tranObj.setTransactionDateTime(realTimeTreeTransactionHistory[2].toString());
            tranObj.setTotalTrees(realTimeTreeTransactionHistory[3].toString() + " TREES");
            tranObj.setTransactionType(realTimeTreeTransactionHistory[4].toString());
            tranObj.setPdNickname(realTimeTreeTransactionHistory[5].toString());
            tranObj.setPdUserId(realTimeTreeTransactionHistory[6].toString());
            tranObj.setPdUserId(realTimeTreeTransactionHistory[6].toString());
            tranObj.setTransactionId(realTimeTreeTransactionHistory[7].toString());
            tranObj.setTransactionStatus(realTimeTreeTransactionHistory[8] != null ? realTimeTreeTransactionHistory[8].toString() : "");
            treeUsageList.add(tranObj);
        }
        return treeUsageList;
    }

}
