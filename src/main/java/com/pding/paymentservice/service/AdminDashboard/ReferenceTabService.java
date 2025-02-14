package com.pding.paymentservice.service.AdminDashboard;

import com.pding.paymentservice.models.ReferralCommission;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferralCommissionHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferredPdDetails;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.repository.WithdrawalRepository;
import com.pding.paymentservice.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReferenceTabService {

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    @Autowired
    WithdrawalService withdrawalService;

    public Page<ReferralCommissionHistory> getReferralCommissionHistoryForAdminDashboard(int page, int size, String searchString) throws Exception {
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> pageObject = otherServicesTablesNativeQueryRepository.getReferralCommissionHistoryForAdminDashboard(pageable, searchString);
        if (pageObject.isEmpty()) return new PageImpl<>(new ArrayList<>(), pageable, 0);

        Integer totalPdReferredInTheSystem = otherServicesTablesNativeQueryRepository.totalNumberOfReferredPdInSystem();
//        Integer pdCountReferredByCurrentPd = otherServicesTablesNativeQueryRepository.totalNumberOfReferredPdByCurrentPd(referralCommissionHistory.getReferrerPdUserId());
        List<ReferralCommissionHistory> referralCommissionHistoryListConcatenated = new ArrayList<>();
        List<ReferralCommissionHistory> referralCommissionHistoryList =
            pageObject.getContent().stream().map(ReferralCommissionHistory::fromObjectArray).collect(Collectors.toList());

        List<String> referrerPdUserIdList = referralCommissionHistoryList.stream().map(ReferralCommissionHistory::getReferrerPdUserId).collect(Collectors.toList());
        //count the number of referred pd by each referrer
        Map<String, Long> pdCountReferredByReferrer = otherServicesTablesNativeQueryRepository.getReferralCountsMap(referrerPdUserIdList);

        referralCommissionHistoryListConcatenated = referralCommissionHistoryList.stream().map(referralCommissionHistory -> {
            Long pdCountReferredByCurrentPd = pdCountReferredByReferrer.get(referralCommissionHistory.getReferrerPdUserId());
            String pdAffiliated = pdCountReferredByCurrentPd.toString() + " / " + totalPdReferredInTheSystem.toString();
            referralCommissionHistory.setPdAffiliated(pdAffiliated);
            return referralCommissionHistory;
        }).collect(Collectors.toList());

        return new PageImpl<>(referralCommissionHistoryListConcatenated, pageable, pageObject.getTotalElements());
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

    private Boolean isReferredPdWithDrawalRequestComplete(String withdrawalId) {
        Optional<Withdrawal> withdrawalOptional = withdrawalService.findById(withdrawalId);

        if (withdrawalOptional.isPresent()) {
            Withdrawal withdrawal = withdrawalOptional.get();
            return withdrawal.getStatus().equals(WithdrawalStatus.COMPLETE);
        }

        return false;
    }
}
