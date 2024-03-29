package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.ViewingHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoPurchaseHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.StatusTabRepository;
import com.pding.paymentservice.repository.admin.ViewingHistoryTabRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ViewingHistoryTabService {
    @Autowired
    ViewingHistoryTabRepository viewingHistoryTabRepository;

    @Autowired
    TokenSigner tokenSigner;

    public ViewingHistory getViewingHistory(String userId, int page, int size) {
        ViewingHistory viewingHistory = new ViewingHistory();

        BigDecimal totalTreesConsumedByUserInCurrentMonth = viewingHistoryTabRepository.totalTreesConsumedByUserInCurrentMonth(userId);
        BigDecimal totalVideosPurchasedInCurrentMonth = viewingHistoryTabRepository.totalVideosPurchasedByUserInCurrentMonth(userId);
        viewingHistory.setTotalVideosViewedInCurrentMonth(new BigDecimal(0));
        viewingHistory.setTreesUsedForVideoPurchaseInLastMonth(totalTreesConsumedByUserInCurrentMonth);
        viewingHistory.setTotalVideosPurchasedInCurrentMonth(totalVideosPurchasedInCurrentMonth);


        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> vphadPage = viewingHistoryTabRepository.findVideoPurchaseHistoryByUserId(userId, pageable);
        List<VideoPurchaseHistoryForAdminDashboard> vphadList = createVideoPurchaseHistoryList(vphadPage.getContent());
        viewingHistory.setVideoPurchaseHistoryForAdminDashboardList(new PageImpl<>(vphadList, pageable, vphadPage.getTotalElements()));
        return viewingHistory;
    }

    public ViewingHistory searchVideo(String userId, String videoTitle, int page, int size) {
        ViewingHistory viewingHistory = new ViewingHistory();

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> vphadPage = viewingHistoryTabRepository.findVideoPurchaseHistoryByUserIdAndVideoTitle(userId, videoTitle, pageable);
        List<VideoPurchaseHistoryForAdminDashboard> vphadList = createVideoPurchaseHistoryList(vphadPage.getContent());
        viewingHistory.setVideoPurchaseHistoryForAdminDashboardList(new PageImpl<>(vphadList, pageable, vphadPage.getTotalElements()));

        return viewingHistory;
    }

    private List<VideoPurchaseHistoryForAdminDashboard> createVideoPurchaseHistoryList(List<Object[]> vphadPage) {
        List<VideoPurchaseHistoryForAdminDashboard> vphadList = new ArrayList<>();
        for (Object innerObject : vphadPage) {
            Object[] videoPurchaseHistory = (Object[]) innerObject;

            String thumbnailPath = "";
            if (!videoPurchaseHistory[2].toString().isEmpty()) {
                thumbnailPath = tokenSigner.signThumbnailUrl(videoPurchaseHistory[2].toString(), 5);
            }

            VideoPurchaseHistoryForAdminDashboard vphadObj = new VideoPurchaseHistoryForAdminDashboard();
            vphadObj.setPurchasedDate(videoPurchaseHistory[0].toString());
            vphadObj.setVideoId(videoPurchaseHistory[1].toString());
            vphadObj.setVideoThumbnail(thumbnailPath);
            vphadObj.setVideoTitle(videoPurchaseHistory[3].toString());
            vphadObj.setPdProfileId(videoPurchaseHistory[4].toString());
            vphadObj.setVideoPrice(videoPurchaseHistory[5].toString());
            vphadList.add(vphadObj);
        }
        return vphadList;
    }
}
