package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.Status;
import com.pding.paymentservice.payload.response.admin.userTabs.StatusForPd;
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
        BigDecimal momPercentage = new BigDecimal(0);
        if (difference.intValue() != 0 && treesAddedInLastMonth.intValue() != 0) {
            momPercentage = difference.divide(treesAddedInLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        status.setTreesAddedInCurrentMonth(treesAddedInLastMonth);
        status.setMom(momPercentage);

        status.setTotalVideosPurchased(statusTabRepository.getTotalVideosPurchasedByUserId(userId));
        return status;
    }

    public StatusForPd getStatusTabDetailsForPd(String pdUserId) {
        StatusForPd statusForPd = new StatusForPd();
        BigDecimal totalExchangedTreesForPd = statusTabRepository.getTotalTreesExchangedByPd(pdUserId);
        BigDecimal totalHoldingTreesForPd = statusTabRepository.getTotalHoldingTreesByPd(pdUserId);
        statusForPd.setExchnagedTrees(totalExchangedTreesForPd);
        statusForPd.setHoldingTrees(totalHoldingTreesForPd);

//        Object[] result1 = statusTabRepository.getTotalTreesSpentOnVideoAndDonationByUserId(userId);
//        BigDecimal totalTreesSpentOnVideos = new BigDecimal(0);
//        BigDecimal totalTreesSpentOnDonation = new BigDecimal(0);
//        if (result1 != null && result1.length > 0) {
//            Object[] innerArray = (Object[]) result1[0];
//            if (innerArray.length > 1) {
//                totalTreesSpentOnVideos = new BigDecimal(innerArray[0].toString());
//                totalTreesSpentOnDonation = new BigDecimal(innerArray[1].toString());
//            }
//        }

        BigDecimal totalTreesEarned = totalExchangedTreesForPd.add(totalHoldingTreesForPd);
        statusForPd.setTotalTrees(totalTreesEarned);

        statusForPd.setTotalTreesEarnedInVideo(statusTabRepository.getVideoSalesTreeForPd(pdUserId));
        statusForPd.setTotalTreesEarnedInDonation(statusTabRepository.getGiftGivingTreeForPd(pdUserId));

        BigDecimal treesEarnedInCurrentMonth = new BigDecimal(0);
        BigDecimal treesEarnedInPrevMonth = new BigDecimal(0);
        treesEarnedInCurrentMonth = statusTabRepository.getCurrentMonthTreeRevenueForPd(pdUserId);
        treesEarnedInPrevMonth = statusTabRepository.getPreviousMonthTreeRevenueForPd(pdUserId);
        statusForPd.setRevenueInCurrentMonth(treesEarnedInCurrentMonth);
        BigDecimal difference = treesEarnedInCurrentMonth.subtract(treesEarnedInPrevMonth);
        BigDecimal momPercentage = new BigDecimal(0);
        if (difference.intValue() != 0 && treesEarnedInPrevMonth.intValue() != 0) {
            momPercentage = difference.divide(treesEarnedInPrevMonth, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        statusForPd.setMom(momPercentage);

        return statusForPd;
    }
}
