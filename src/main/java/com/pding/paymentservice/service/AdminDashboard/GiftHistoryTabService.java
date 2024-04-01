package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.GiftHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.DonationHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.GiftHistoryTabRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        giftHistory.setTotalTreesDonatedInCurrentMonth(new BigDecimal(0));

        List<DonationHistoryForAdminDashboard> dhadList = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> dhadPage = giftHistoryTabRepository.findDonationHistoryByUserId(userId, pageable);
        for (Object innerObject : dhadPage.getContent()) {
            Object[] giftDonationHistory = (Object[]) innerObject;
            DonationHistoryForAdminDashboard dhadObj = new DonationHistoryForAdminDashboard();
            dhadObj.setDateTime(giftDonationHistory[0].toString());
            dhadObj.setPdProfileId(giftDonationHistory[1].toString());
            dhadObj.setTreesOrLeafs(giftDonationHistory[2].toString());
            dhadObj.setAmount(giftDonationHistory[3].toString());
            dhadList.add(dhadObj);
        }

        giftHistory.setDonationHistoryForAdminDashboardList(new PageImpl<>(dhadList, pageable, dhadPage.getTotalElements()));
        return giftHistory;
    }
}
