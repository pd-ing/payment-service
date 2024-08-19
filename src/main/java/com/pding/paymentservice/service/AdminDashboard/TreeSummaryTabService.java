package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.TreeSummary;
import com.pding.paymentservice.payload.response.UserObject;
import com.pding.paymentservice.payload.response.admin.TreeSummaryGridResult;
import com.pding.paymentservice.repository.admin.TreeSummaryTabRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TreeSummaryTabService {
    @Autowired
    TreeSummaryTabRepository treeSummaryTabRepository;

    @Autowired
    TokenSigner tokenSigner;


    public TreeSummaryGridResult getTreesSummaryForAllUsers(LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        TreeSummaryGridResult treeSummaryGridResult = new TreeSummaryGridResult();
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> userPage = treeSummaryTabRepository.getTreesRevenueForPd(startDate, endDate, searchString, pageable);
        List<UserObject> userTreeSummaryList = createTreeSummaryList(userPage.getContent(), startDate, endDate);
        treeSummaryGridResult.setUserObjects(new PageImpl<>(userTreeSummaryList, pageable, userPage.getTotalElements()));
        return treeSummaryGridResult;
    }

    public TreeSummary getTreesSummaryTotals(LocalDate startDate, LocalDate endDate, String searchString) {
        TreeSummary treeSummary = new TreeSummary();
        BigDecimal totalTreeRevenue = treeSummaryTabRepository.getTotalTreesConsumedForVideos(startDate, endDate, searchString).add(treeSummaryTabRepository.getTotalDonatedTrees(startDate, endDate, searchString));
        BigDecimal totalTreesExchanged = treeSummaryTabRepository.getTotalExchangedTreesForAllUsers(startDate, endDate, searchString);
        BigDecimal totalUnexchangedTrees = treeSummaryTabRepository.getUnExchangedTreesForAllUsers(startDate, endDate, searchString);
        treeSummary.setTotalTreeRevenue(totalTreeRevenue);
        treeSummary.setTotalTreesExchanged(totalTreesExchanged);
        treeSummary.setTotalUnexchangedTrees(totalUnexchangedTrees);
        return treeSummary;
    }

    public TreeSummary getTreesSummaryForPd(String pdId) {
        TreeSummary treeSummary = new TreeSummary();
        BigDecimal videoPurchaseTrees = treeSummaryTabRepository.getTotalTreesConsumedForVideosByPd(pdId, null, null);
        BigDecimal totalTreesDonated = treeSummaryTabRepository.getTreesDonatedForPd(pdId, null, null);
        BigDecimal totalTreeRevenue =videoPurchaseTrees.add(totalTreesDonated);
        BigDecimal totalTreesExchanged = treeSummaryTabRepository.getTotalExchangedTreesForPd(pdId, null, null);
        BigDecimal totalUnexchangedTrees = treeSummaryTabRepository.getTotalUnExchangedTreesForPd(pdId, null, null);

        //get start date & end date of last month
        LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth());

        BigDecimal lastMonthVideoPurchaseTrees = treeSummaryTabRepository.getTotalTreesConsumedForVideosByPd(pdId, startDate, endDate);
        BigDecimal lastMonthtotalTreesDonated = treeSummaryTabRepository.getTreesDonatedForPd(pdId, startDate, endDate);
        BigDecimal lastMonthTreeRevenue = lastMonthVideoPurchaseTrees.add(lastMonthtotalTreesDonated);

        //get start date & end date of this month
        startDate = LocalDate.now().withDayOfMonth(1);
        endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal currentMonthVideoPurchaseTrees = treeSummaryTabRepository.getTotalTreesConsumedForVideosByPd(pdId, startDate, endDate);
        BigDecimal currentMonthtotalTreesDonated = treeSummaryTabRepository.getTreesDonatedForPd(pdId, startDate, endDate);
        BigDecimal currentMonthTreeRevenue = currentMonthVideoPurchaseTrees.add(currentMonthtotalTreesDonated);

        //get month-over-month percent growth
        BigDecimal monthOverMonthPercentGrowth = BigDecimal.ZERO;
        if (lastMonthTreeRevenue.compareTo(BigDecimal.ZERO) != 0) {
            monthOverMonthPercentGrowth = currentMonthTreeRevenue.subtract(lastMonthTreeRevenue).divide(lastMonthTreeRevenue, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        }

        treeSummary.setTotalTreeRevenue(totalTreeRevenue);
        treeSummary.setTotalTreesExchanged(totalTreesExchanged);
        treeSummary.setTotalUnexchangedTrees(totalUnexchangedTrees);
        treeSummary.setTotalTreesDonated(totalTreesDonated);
        treeSummary.setVideoPurchaseTrees(videoPurchaseTrees);
        treeSummary.setLastMonthTreeRevenue(lastMonthTreeRevenue);
        treeSummary.setCurrentMonthTreeRevenue(currentMonthTreeRevenue);
        treeSummary.setMonthOverMonthPercentGrowth(monthOverMonthPercentGrowth);
        return treeSummary;
    }


    private List<UserObject> createTreeSummaryList(List<Object[]> userPage, LocalDate startDate, LocalDate endDate) {
        List<UserObject> userList = new ArrayList<>();
        for (Object innerObject : userPage) {
            BigDecimal totalTreesEarned;
            BigDecimal totalTreesDonated;
            BigDecimal totalTreesExchanged;
            BigDecimal totalTreesUnexchanged;
            String userId;
            Object[] treeSummaryByUser = (Object[]) innerObject;
            UserObject userObj = new UserObject();
            userId = treeSummaryByUser[0].toString();
            userObj.setPdUserId(userId);
            userObj.setNickName(treeSummaryByUser[1].toString());
            userObj.setEmail(treeSummaryByUser[2].toString());
            userObj.setPdType(treeSummaryByUser[3].toString());
            totalTreesEarned = (BigDecimal) treeSummaryByUser[4];
            totalTreesDonated = treeSummaryTabRepository.getTreesDonatedForPd(userId, startDate, endDate);
            totalTreesExchanged = treeSummaryTabRepository.getTotalExchangedTreesForPd(userId, startDate, endDate);
            totalTreesUnexchanged = treeSummaryTabRepository.getTotalUnExchangedTreesForPd(userId, startDate, endDate);
            TreeSummary treeSummary = new TreeSummary();
            treeSummary.setTotalTreeRevenue(totalTreesEarned.add(totalTreesDonated));
            treeSummary.setTotalTreesExchanged(totalTreesExchanged);
            treeSummary.setTotalUnexchangedTrees(totalTreesUnexchanged);
            userObj.setTreeSummary(treeSummary);
            userList.add(userObj);
        }
        return userList;
    }

}
