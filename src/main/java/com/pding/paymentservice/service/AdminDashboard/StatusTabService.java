package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.Status;
import com.pding.paymentservice.repository.admin.StatusTabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StatusTabService {

    @Autowired
    StatusTabRepository statusTabRepository;

    public Status getStatusTabDetails(String userId) {
        Status status = new Status();
        status.setTotalTreesCharged(statusTabRepository.getTotalTreesChargedByUserId(userId));
        status.setCurrentHoldingTrees(statusTabRepository.getCurrentHoldingTreesByUserId(userId));

        Object[] result1 = statusTabRepository.getTotalTreesSpentOnVideoAndDonationByUserId(userId);
        BigDecimal totalTreesSpentOnVideos = new BigDecimal(0);
        BigDecimal totalTreesSpentOnDonation = new BigDecimal(0);
        if (result1 != null && result1.length > 0) {
            Object[] innerArray = (Object[]) result1[0];
            if (innerArray.length > 1) {
                totalTreesSpentOnVideos = new BigDecimal(innerArray[0].toString());
                totalTreesSpentOnDonation = new BigDecimal(innerArray[1].toString());
            }
        }

        BigDecimal totalTreesSpent = totalTreesSpentOnVideos.add(totalTreesSpentOnDonation);

        status.setTotalTreesSpendInVideoPurchase(totalTreesSpentOnVideos);
        status.setTotalTreesDonated(totalTreesSpentOnDonation);
        status.setTotalTreesSpent(totalTreesSpent);

        Object[] result2 = statusTabRepository.getMonthlyTreesPurchased(userId);
        BigDecimal treesAddedInCurrentMonth = new BigDecimal(0);
        BigDecimal treesAddedInLastMonth = new BigDecimal(0);
        if (result2 != null && result2.length > 0) {
            Object[] innerArray = (Object[]) result2[0];
            if (innerArray.length > 1) {
                treesAddedInCurrentMonth = new BigDecimal(innerArray[0].toString());
                treesAddedInLastMonth = new BigDecimal(innerArray[1].toString());
            }
        }
        BigDecimal difference = treesAddedInCurrentMonth.subtract(treesAddedInLastMonth);
        BigDecimal momPercentage = difference.divide(treesAddedInLastMonth, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        status.setTreesAddedInCurrentMonth(treesAddedInLastMonth);
        status.setMom(momPercentage);

        status.setTotalVideosPurchased(statusTabRepository.getTotalVideosPurchasedByUserId(userId));
        return status;
    }
}
