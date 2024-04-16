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
        Pageable pageable = PageRequest.of(page, size, Sort.by("email").ascending());
        Page<Object[]> userPage = treeSummaryTabRepository.getTreesSummaryByUsers(startDate, endDate, searchString, pageable);
        List<UserObject> userTreeSummaryList = createTreeSummaryList(userPage.getContent());
        treeSummaryGridResult.setUserObjects(new PageImpl<>(userTreeSummaryList, pageable, userPage.getTotalElements()));
        return treeSummaryGridResult;
    }

    public TreeSummary getTreesSummaryTotals() {
        TreeSummary treeSummary = new TreeSummary();
        BigDecimal totalTreeRevenue = treeSummaryTabRepository.getTotalTreeRevenueForAllUsers();
        BigDecimal totalTreesExchanged = treeSummaryTabRepository.getTotalExchangedTreesForAllUsers();
        BigDecimal totalUnexchangedTrees = treeSummaryTabRepository.getUnExchangedTreesForAllUsers();
        treeSummary.setTotalTreeRevenue(totalTreeRevenue);
        treeSummary.setTotalTreesExchanged(totalTreesExchanged);
        treeSummary.setTotalUnexchangedTrees(totalUnexchangedTrees);
        return treeSummary;
    }



    private List<UserObject> createTreeSummaryList(List<Object[]> userPage) {
        List<UserObject> userList = new ArrayList<>();
        for (Object innerObject : userPage) {
            Object[] treeSummaryByUser = (Object[]) innerObject;
            UserObject userObj = new UserObject();
            TreeSummary treeSummary = new TreeSummary();
            treeSummary.setTotalTreeRevenue((BigDecimal) treeSummaryByUser[3]);
            treeSummary.setTotalTreesExchanged((BigDecimal) treeSummaryByUser[4]);
            treeSummary.setTotalUnexchangedTrees((BigDecimal) treeSummaryByUser[5]);
            userObj.setTreeSummary(treeSummary);
            userObj.setNickName(treeSummaryByUser[0].toString());
            userObj.setEmail(treeSummaryByUser[1].toString());
            userObj.setPdType(treeSummaryByUser[2].toString());
            userList.add(userObj);
        }
        return userList;
    }

}
