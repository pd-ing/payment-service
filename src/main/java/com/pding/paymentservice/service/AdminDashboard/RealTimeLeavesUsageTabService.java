package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.RealTimeLeafTransactionHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.TotalLeavesUsageSummary;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.TransactionHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.RealTimeLeavesUsageTabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RealTimeLeavesUsageTabService {

    @Autowired
    RealTimeLeavesUsageTabRepository realTimeLeavesUsageTabRepository;

    public TotalLeavesUsageSummary getLeavesSummaryTotals(LocalDate startDate, LocalDate endDate) {
        TotalLeavesUsageSummary leavesSummary = new TotalLeavesUsageSummary();

        BigDecimal totalLeavesUsedForVideoCall = realTimeLeavesUsageTabRepository.getTotalLeavesUsedForCall("VIDEO_CALL", startDate, endDate);
        BigDecimal totalLeavesUsedForVoiceCall = realTimeLeavesUsageTabRepository.getTotalLeavesUsedForCall("AUDIO_CALL", startDate, endDate);
        BigDecimal totalLeavesUsedForChat = realTimeLeavesUsageTabRepository.getTotalLeavesUsedForChat(startDate, endDate);
        BigDecimal totalLeavesUsedForGift = realTimeLeavesUsageTabRepository.getTotalGiftInChat(startDate, endDate)
                .add(realTimeLeavesUsageTabRepository.getTotalGiftInCall(startDate, endDate));
        BigDecimal totalLeavesUsedForMediaTrading = realTimeLeavesUsageTabRepository.getTotalLeavesUsedForInChatMediaTrading(startDate, endDate);

        BigDecimal totalLeavesTransacted = totalLeavesUsedForVideoCall
                .add(totalLeavesUsedForVoiceCall)
                .add(totalLeavesUsedForChat)
                .add(totalLeavesUsedForGift)
                .add(totalLeavesUsedForMediaTrading);

        leavesSummary.setTotalLeavesTransacted(totalLeavesTransacted);
        leavesSummary.setTotalLeavesUsedForVideoCall(totalLeavesUsedForVideoCall);
        leavesSummary.setTotalLeavesUsedForVoiceCall(totalLeavesUsedForVoiceCall);
        leavesSummary.setTotalLeavesUsedForChat(totalLeavesUsedForChat);
        leavesSummary.setTotalLeavesUsedForGift(totalLeavesUsedForGift);
        leavesSummary.setTotalLeavesUsedForInChatMediaBuying(totalLeavesUsedForMediaTrading);
        return leavesSummary;
    }


    public RealTimeLeafTransactionHistory getRealTimeLeafsUsage(LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        RealTimeLeafTransactionHistory result = new RealTimeLeafTransactionHistory();
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDateTime").descending());
        if(searchString == null || searchString.isEmpty()){
            searchString = null;
        }
        Page<Object[]> transaction = realTimeLeavesUsageTabRepository.getRealTimeTreeUsage(startDate, endDate, searchString, pageable);
        List<TransactionHistoryForAdminDashboard> transactionList = createLeafUsageSummaryList(transaction.getContent());
        result.setTransactionHistoryForAdminDashboards(new PageImpl<>(transactionList, pageable, transaction.getTotalElements()));
        return result;
    }

    private List<TransactionHistoryForAdminDashboard> createLeafUsageSummaryList(List<Object[]> transactionPage) {
        List<TransactionHistoryForAdminDashboard> leafUsageList = new ArrayList<>();
        for (Object innerObject : transactionPage) {
            Object[] realTimeTreeTransactionHistory = (Object[]) innerObject;
            TransactionHistoryForAdminDashboard tranObj = new TransactionHistoryForAdminDashboard();
            tranObj.setUserId(realTimeTreeTransactionHistory[0].toString());
            tranObj.setUserEmail(realTimeTreeTransactionHistory[1].toString());
            tranObj.setTransactionDateTime(realTimeTreeTransactionHistory[2].toString());
            tranObj.setTransactionType(realTimeTreeTransactionHistory[3].toString());
            tranObj.setTotalLeaves(realTimeTreeTransactionHistory[4].toString());
            tranObj.setPdUserId(realTimeTreeTransactionHistory[5].toString());
            tranObj.setPdNickname(realTimeTreeTransactionHistory[6].toString());

            leafUsageList.add(tranObj);
        }
        return leafUsageList;
    }
}
