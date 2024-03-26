package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.ViewingHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoPurchaseHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.StatusTabRepository;
import com.pding.paymentservice.repository.admin.ViewingHistoryTabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ViewingHistoryTabService {
    @Autowired
    ViewingHistoryTabRepository viewingHistoryTabRepository;

    public ViewingHistory getViewingHistory(String userId) {
        ViewingHistory viewingHistory = new ViewingHistory();

        BigDecimal totalTreesConsumedByUserInCurrentMonth = viewingHistoryTabRepository.totalTreesConsumedByUserInCurrentMonth(userId);
        BigDecimal totalVideosPurchasedInCurrentMonth = viewingHistoryTabRepository.totalVideosPurchasedByUserInCurrentMonth(userId);
        viewingHistory.setTotalVideosViewedInCurrentMonth(new BigDecimal(0));
        viewingHistory.setTreesUsedForVideoPurchaseInLastMonth(totalTreesConsumedByUserInCurrentMonth);
        viewingHistory.setTotalVideosPurchasedInCurrentMonth(totalVideosPurchasedInCurrentMonth);

        List<VideoPurchaseHistoryForAdminDashboard> vphadList = new ArrayList<>();
        List<Object[]> videoPurchaseHistoryForAdminDashboardObjectList = viewingHistoryTabRepository.findVideoPurchaseHistoryByUserId(userId);
        for (Object innerObject : videoPurchaseHistoryForAdminDashboardObjectList) {
            Object[] videoPurchaseHistory = (Object[]) innerObject;

            VideoPurchaseHistoryForAdminDashboard vphadObj = new VideoPurchaseHistoryForAdminDashboard();
            vphadObj.setPurchasedDate(videoPurchaseHistory[0].toString());
            vphadObj.setVideoId(videoPurchaseHistory[1].toString());
            vphadObj.setVideoThumbnail(videoPurchaseHistory[2].toString());
            vphadObj.setVideoTitle(videoPurchaseHistory[3].toString());
            vphadObj.setPdProfileId(videoPurchaseHistory[4].toString());
            vphadObj.setVideoPrice(videoPurchaseHistory[5].toString());
            vphadList.add(vphadObj);
        }

        viewingHistory.setVideoPurchaseHistoryForAdminDashboardList(vphadList);
        return viewingHistory;
    }
}
