package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.GiftHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.GiftHistoryForPd;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.DonationHistoryForAdminDashboard;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoSalesHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.GiftHistoryTabRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GiftHistoryTabService {
    @Autowired
    GiftHistoryTabRepository giftHistoryTabRepository;

    @Autowired
    TokenSigner tokenSigner;

    public GiftHistory getGiftHistoryTabDetails(String userId, int page, int size) {
        GiftHistory giftHistory = new GiftHistory();

        BigDecimal totalTreesDonatedByUserInCurrentMonth = giftHistoryTabRepository.totalTreesDonatedByUserInCurrentMonth(userId);
        giftHistory.setTotalTreesDonatedInCurrentMonth(totalTreesDonatedByUserInCurrentMonth);

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> dhadPage = giftHistoryTabRepository.findDonationHistoryByUserId(userId, pageable);
        List<DonationHistoryForAdminDashboard> dhadList = createGiftHistoryList(dhadPage.getContent());
        // List<DonationHistoryForAdminDashboard> dhadList = new ArrayList<>();
        /* for (Object innerObject : dhadPage.getContent()) {
            Object[] giftDonationHistory = (Object[]) innerObject;
            DonationHistoryForAdminDashboard dhadObj = new DonationHistoryForAdminDashboard();
            dhadObj.setDateTime(giftDonationHistory[0].toString());
            dhadObj.setPdProfileId(giftDonationHistory[1].toString());
            dhadObj.setTreesOrLeafs(giftDonationHistory[2].toString());
            dhadObj.setAmount(giftDonationHistory[3].toString());
            dhadList.add(dhadObj);
        } */
        giftHistory.setDonationHistoryForAdminDashboardList(new PageImpl<>(dhadList, pageable, dhadPage.getTotalElements()));
        return giftHistory;
    }

    public GiftHistoryForPd getGiftHistoryTabForPdDetails(String pdUserId, LocalDate startDate, LocalDate endDate, int page, int size) {
        GiftHistoryForPd giftHistoryForPd = new GiftHistoryForPd();

        BigDecimal totalTreesGifted = giftHistoryTabRepository.totalTreesReceivedByPd(pdUserId);
        giftHistoryForPd.setTotalTreesGifted(totalTreesGifted);

        Pageable pageable = PageRequest.of(page, size, Sort.by("last_update_date").descending());
        Page<Object[]> dhadPage = giftHistoryTabRepository.findDonationHistoryByPdId(pdUserId, startDate, endDate, pageable);
        List<DonationHistoryForAdminDashboard> dhadList = createGiftHistoryList(dhadPage.getContent());
        giftHistoryForPd.setDonationHistoryForAdminDashboardList(new PageImpl<>(dhadList, pageable, dhadPage.getTotalElements()));
        return giftHistoryForPd;
    }

    private List<DonationHistoryForAdminDashboard> createGiftHistoryList(List<Object[]> dhadPage) {
        List<DonationHistoryForAdminDashboard> dhadList = new ArrayList<>();
        for (Object innerObject : dhadPage) {
            Object[] giftDonationHistory = (Object[]) innerObject;
            DonationHistoryForAdminDashboard dhadObj = new DonationHistoryForAdminDashboard();
            dhadObj.setDateTime(giftDonationHistory[0].toString());
            dhadObj.setPdProfileId(giftDonationHistory[1].toString());
            dhadObj.setTreesOrLeafs(giftDonationHistory[2].toString());
            dhadObj.setAmount(giftDonationHistory[3].toString());
            dhadObj.setPdEmailId(giftDonationHistory.length > 4 && giftDonationHistory[4] != null ? giftDonationHistory[4].toString() : "");
            dhadList.add(dhadObj);
        }
        return dhadList;
    }
}
