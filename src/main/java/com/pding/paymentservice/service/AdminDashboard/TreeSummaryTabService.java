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
