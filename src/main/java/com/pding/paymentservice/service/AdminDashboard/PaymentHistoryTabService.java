package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.PaymentHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.ViewingHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.PaymentHistoryForAdminDashboard;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.VideoPurchaseHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.PaymentHistoryTabRepository;
import com.pding.paymentservice.util.CommonMethods;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentHistoryTabService {
    @Autowired
    PaymentHistoryTabRepository paymentHistoryTabRepository;

    @Autowired
    TokenSigner tokenSigner;


    public PaymentHistory getPaymentHistory(String userId, int page, int size) {
        PaymentHistory paymentHistory = new PaymentHistory();
        BigDecimal numberOfTreesChargedInCurrentMonth = paymentHistoryTabRepository.numberOfTreesChargedInCurrentMonth(userId);
        paymentHistory.setNumberOfTreesChargedInCurrentMonth(numberOfTreesChargedInCurrentMonth);
        String userStripeID = paymentHistoryTabRepository.userStripeID(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> phadPage = paymentHistoryTabRepository.findPaymentHistoryByUserId(userId, pageable);
        List<PaymentHistoryForAdminDashboard> phadList = createPaymentHistoryList(phadPage.getContent(), userStripeID);
        paymentHistory.setPaymentHistoryForAdminDashboardList(new PageImpl<>(phadList, pageable, phadPage.getTotalElements()));
        return paymentHistory;
    }

    public PaymentHistory getPaymentHistoryAllUsers(LocalDate startDate, LocalDate endDate, int sortOrder, int page, int size) {
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setNumberOfTreesChargedInCurrentMonth(BigDecimal.valueOf(0.0)); // Default value
        String userStripeID = ""; //Keep empty for all users
        Pageable pageable;
        if(sortOrder == 0){
            pageable = PageRequest.of(page, size, Sort.by("purchase_date").ascending());
        }
        else{
            pageable = PageRequest.of(page, size, Sort.by("purchase_date").descending());
        }
         Page<Object[]> phadPage = paymentHistoryTabRepository.getPaymentHistoryForAllUsers(startDate, endDate, pageable);
        List<PaymentHistoryForAdminDashboard> phadList = createPaymentHistoryList(phadPage.getContent(), userStripeID);
        paymentHistory.setPaymentHistoryForAdminDashboardList(new PageImpl<>(phadList, pageable, phadPage.getTotalElements()));
        return paymentHistory;
    }

    public PaymentHistory searchByEmail(String searchString, int page, int size) {
        PaymentHistory paymentHistory = new PaymentHistory();
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> phadPage = paymentHistoryTabRepository.findPaymentHistoryByEmailId(searchString, pageable);
        List<PaymentHistoryForAdminDashboard> phadList = createPaymentHistoryList(phadPage.getContent(), "");
        paymentHistory.setPaymentHistoryForAdminDashboardList(new PageImpl<>(phadList, pageable, phadPage.getTotalElements()));
        return paymentHistory;
    }

    private List<PaymentHistoryForAdminDashboard> createPaymentHistoryList(List<Object[]> phadPage, String userStripeID) {
        List<PaymentHistoryForAdminDashboard> phadList = new ArrayList<>();
        Double purchasedTrees = null;
        Double purchasedLeafs = null;
        final String DEFAULT_STRIPE_ID = "Stripe ID not set";
        for (Object innerObject : phadPage) {
            Object[] paymentHistory = (Object[]) innerObject;
            PaymentHistoryForAdminDashboard phadObj = new PaymentHistoryForAdminDashboard();
            purchasedTrees = Double.parseDouble(paymentHistory[1].toString());
            purchasedLeafs = Double.parseDouble(paymentHistory[2].toString());
            phadObj.setStripeId(userStripeID == null ? DEFAULT_STRIPE_ID : userStripeID);
            phadObj.setEmail(paymentHistory[4].toString());
            phadObj.setPurchaseDate(paymentHistory[0].toString());
            phadObj.setTreeOrLeaf(purchasedLeafs > 0 ? "Leaf" : purchasedTrees > 0 ? "Tree" : " ");
            phadObj.setAmount(purchasedLeafs > 0 ? paymentHistory[2].toString() : paymentHistory[1].toString());
            phadObj.setAmountInDollarsWithTax(CommonMethods.calculateFeeAndTax(paymentHistory[3].toString()));
            phadList.add(phadObj);
        }
        return phadList;
    }

}
