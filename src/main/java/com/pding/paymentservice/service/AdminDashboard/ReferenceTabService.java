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

        List<ReferralCommissionHistory> referralCommissionHistoryList = new ArrayList<>();
        List<ReferralCommissionHistory> referralCommissionHistoryListConcatenated = new ArrayList<>();
        for (Object[] innerObject : pageObject.getContent()) {
            ReferralCommissionHistory referralCommissionHistory = ReferralCommissionHistory.fromObjectArray(innerObject);

            // Show/Return the details of the referral commission only if the withdrawal request of the referredPD has been completed.
            if (isReferredPdWithDrawalRequestComplete(referralCommissionHistory.getWithdrawalId())) {
                Integer pdCountReferredByCurrentPd = otherServicesTablesNativeQueryRepository.totalNumberOfReferredPdByCurrentPd(referralCommissionHistory.getReferrerPdUserId());
                Integer totalPdReferredInTheSystem = otherServicesTablesNativeQueryRepository.totalNumberOfReferredPdInSystem();

                String pdAffiliated = pdCountReferredByCurrentPd.toString() + " / " + totalPdReferredInTheSystem.toString();

                referralCommissionHistory.setPdAffiliated(pdAffiliated);

                referralCommissionHistoryList.add(referralCommissionHistory);
            }
        }

        List<String> referrerPdIds = new ArrayList<>();

        for (ReferralCommissionHistory item : referralCommissionHistoryList){
            String referrerPdId = item.getReferrerPdUserId();

            if(referrerPdIds.contains(referrerPdId))
                continue;

            referrerPdIds.add(referrerPdId);
            List<ReferralCommissionHistory> filteredList = referralCommissionHistoryList.stream()
                    .filter(obj -> obj.getReferrerPdUserId().equals(referrerPdId))
                    .collect(Collectors.toList());

            ReferralCommissionHistory refch = null;

            for(ReferralCommissionHistory temp : filteredList){
                if(refch == null) {
                    // first object in te list
                    refch = temp;
                }
                else {
                    //concatenate results
                    refch.setReferralCommissionId(refch.getReferralCommissionId() + ", " + temp.getReferralCommissionId());
                    refch.setWithdrawalId(refch.getWithdrawalId() + ", " + temp.getWithdrawalId());
                    refch.setReferrerCommissionAmountInTrees(new BigDecimal(refch.getReferrerCommissionAmountInTrees())
                            .add(new BigDecimal(temp.getReferrerCommissionAmountInTrees()))
                            .toString());
                    refch.setReferrerCommissionAmountInLeafs(new BigDecimal(refch.getReferrerCommissionAmountInLeafs())
                            .add(new BigDecimal(temp.getReferrerCommissionAmountInLeafs()))
                            .toString());
                    refch.setReferrerCommissionCreatedDate(refch.getReferrerCommissionCreatedDate() + ", " + temp.getReferrerCommissionCreatedDate());
                    refch.setReferrerCommissionUpdatedDate(refch.getReferrerCommissionUpdatedDate() + ", " + temp.getReferrerCommissionUpdatedDate());
                    refch.setReferrerCommissionTransferStatus(refch.getReferrerCommissionTransferStatus() + ", " + temp.getReferrerCommissionTransferStatus());
                    refch.setReferredPdUserId(refch.getReferredPdUserId() + ", " + temp.getReferredPdUserId());
                }
            }
            referralCommissionHistoryListConcatenated.add(refch);
        }

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
