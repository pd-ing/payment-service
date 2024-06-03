package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.models.ReferralCommission;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferralCommissionHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferredPdDetails;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReferenceTabService {

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    public Page<ReferralCommissionHistory> getReferralCommissionHistoryForAdminDashboard(int page, int size, String searchString) throws Exception {
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> pageObject = otherServicesTablesNativeQueryRepository.getReferralCommissionHistoryForAdminDashboard(pageable, searchString);

        List<ReferralCommissionHistory> referralCommissionHistoryList = new ArrayList<>();
        for (Object[] innerObject : pageObject.getContent()) {
            ReferralCommissionHistory referralCommissionHistory = ReferralCommissionHistory.fromObjectArray(innerObject);
            Integer pdCountReferredByCurrentPd = otherServicesTablesNativeQueryRepository.totalNumberOfReferredPdByCurrentPd(referralCommissionHistory.getReferrerPdUserId());
            Integer totalPdReferredInTheSystem = otherServicesTablesNativeQueryRepository.totalNumberOfReferredPdInSystem();

            String pdAffiliated = pdCountReferredByCurrentPd.toString() + " / " + totalPdReferredInTheSystem.toString();

            referralCommissionHistory.setPdAffiliated(pdAffiliated);

            referralCommissionHistoryList.add(referralCommissionHistory);
        }

        return new PageImpl<>(referralCommissionHistoryList, pageable, pageObject.getTotalElements());
    }

    public Page<ReferredPdDetails> getReferredPdDetails(String referrerPdUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> pageObject = otherServicesTablesNativeQueryRepository.getReferredPdDetails(referrerPdUserId, pageable);

        List<ReferredPdDetails> referredPdDetailsList = new ArrayList<>();

        for (Object[] innerObject : pageObject.getContent()) {
            ReferredPdDetails referredPdDetails = ReferredPdDetails.fromObjectArray(innerObject);
            referredPdDetailsList.add(referredPdDetails);
        }

        return new PageImpl<>(referredPdDetailsList, pageable, pageObject.getTotalElements());
    }

}
