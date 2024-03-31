package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.payload.response.admin.userTabs.PaymentHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.PaymentHistoryForAdminDashboard;
import com.pding.paymentservice.repository.admin.PaymentHistoryTabRepository;
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
        Page<Object[]> phadPage = paymentHistoryTabRepository.findPayentHistoryByUserId(userId, pageable);
        List<PaymentHistoryForAdminDashboard> phadList = createPaymentHistoryList(phadPage.getContent(), userStripeID);
        paymentHistory.setPaymentHistoryForAdminDashboardList(new PageImpl<>(phadList, pageable, phadPage.getTotalElements()));
        return paymentHistory;
    }

    private List<PaymentHistoryForAdminDashboard> createPaymentHistoryList(List<Object[]> phadPage, String userStripeID) {
        List<PaymentHistoryForAdminDashboard> phadList = new ArrayList<>();
        for (Object innerObject : phadPage) {
            Object[] paymemtHistory = (Object[]) innerObject;
            PaymentHistoryForAdminDashboard phadObj = new PaymentHistoryForAdminDashboard();
            phadObj.setStripeId(userStripeID);
            phadObj.setPurchaseDate(paymemtHistory[0].toString());
            phadObj.setTreeOrLeaf(paymemtHistory[1] != null ? "Tree" : paymemtHistory[2] != null? "Leaf" : " ");
            phadObj.setAmount(paymemtHistory[1] != null? paymemtHistory[1].toString() : paymemtHistory[2].toString());
            phadObj.setAmountInDollarsWithTax(paymemtHistory[3].toString());
            phadList.add(phadObj);
        }
        return phadList;
    }
}
