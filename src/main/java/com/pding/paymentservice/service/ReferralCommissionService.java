package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.ReferralCommission;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.CommissionTransferStatus;
import com.pding.paymentservice.models.other.services.tables.dto.ReferralCommissionDetailsDTO;
import com.pding.paymentservice.models.other.services.tables.dto.ReferralInfoDTO;
import com.pding.paymentservice.models.other.services.tables.dto.UserInfoDTO;
import com.pding.paymentservice.payload.response.PayReferrerThroughStripeResponse;
import com.pding.paymentservice.models.other.services.tables.dto.ReferredPdDetailsDTO;
import com.pding.paymentservice.payload.response.referralTab.ReferredPDDetailsRecord;
import com.pding.paymentservice.payload.response.referralTab.ReferrerPDDetailsRecord;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.repository.ReferralCommissionRepository;
import com.pding.paymentservice.paymentclients.stripe.StripeClient;
import com.pding.paymentservice.util.LogSanitizer;
import com.stripe.model.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.pding.paymentservice.PdLogger.EVENT.GIVE_REFERRAL_COMMISSION;

@Service
@Slf4j
public class ReferralCommissionService {
    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    @Autowired
    ReferralCommissionRepository referralCommissionRepository;

    @Autowired
    StripeClient stripeClient;

    @Autowired
    PdLogger pdLogger;

    long valueOfOneTreeInCents = 10L;


    @Transactional
    public void createReferralCommissionEntryInPendingState(Withdrawal withdrawal) throws Exception {
        ReferralInfoDTO referralInfoDTO = getReferralInfo(withdrawal.getPdUserId());
        if (referralInfoDTO != null) {
            UserInfoDTO referrerPdUserInfoDTO = getUserInfo(referralInfoDTO.getReferrerPdUserId());
            String commissionAmountInTrees = getCommissionAmountInTrees(withdrawal.getTrees(), referrerPdUserInfoDTO.getCommissionPercent());
            String commissionAmountInLeafs = getCommissionAmountInLeafs(withdrawal.getLeafs(), referrerPdUserInfoDTO.getCommissionPercent());
            BigDecimal commissionAmountInCents = (new BigDecimal(commissionAmountInTrees).add(new BigDecimal(commissionAmountInLeafs)))
                .multiply(new BigDecimal(Long.toString(valueOfOneTreeInCents)));

            ReferralCommission referralCommission = createReferralCommission(
                    withdrawal,
                    referralInfoDTO,
                    referrerPdUserInfoDTO,
                    commissionAmountInTrees,
                    commissionAmountInLeafs,
                    commissionAmountInCents
            );
            referralCommissionRepository.save(referralCommission);
        }
    }

    @Transactional
    public String updateReferralCommissionEntryToCompletedState(String referralCommissionId) throws Exception {
        Optional<ReferralCommission> referralCommissionOptional = referralCommissionRepository.findById(referralCommissionId);
        if (referralCommissionOptional.isEmpty()) {
            log.error("Commission entry not found for the given commission id: {}", LogSanitizer.sanitizeForLog(referralCommissionId));
            return "Commission entry not found for the given commission id";
        }

        ReferralCommission referralCommission = referralCommissionOptional.get();

        referralCommission.setCommissionTransferStatus(CommissionTransferStatus.TRANSFER_COMPLETED);
        referralCommission.setUpdatedDate(LocalDateTime.now());

        referralCommissionRepository.save(referralCommission);
        log.info("Successfully updated the state to {} for referralCommissionId {}", LogSanitizer.sanitizeForLog(CommissionTransferStatus.TRANSFER_COMPLETED.getDisplayName()), LogSanitizer.sanitizeForLog(referralCommissionId));
        return "Successfully updated the state to " + CommissionTransferStatus.TRANSFER_COMPLETED;
    }


    @Transactional
    public Page<ReferredPdDetailsDTO> getDetailsOfAllTheReferredPd(String referrerPdUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> referredPdDetailsPage = otherServicesTablesNativeQueryRepository.getDetailsOfAllTheReferredPd(referrerPdUserId, pageable);

        List<ReferredPdDetailsDTO> referredPdDetailsDTOList = new ArrayList<>();
        for (Object[] referredPdObj : referredPdDetailsPage.getContent()) {
            ReferredPdDetailsDTO referredPdDetailsDTO = ReferredPdDetailsDTO.fromObjectArray(referredPdObj);
            referredPdDetailsDTOList.add(referredPdDetailsDTO);
        }

        return new PageImpl<>(referredPdDetailsDTOList, pageable, referredPdDetailsPage.getTotalElements());
    }

    @Transactional
    public Page<ReferredPDDetailsRecord> listReferredPdDetailsEOL(String referrerPdUserId, LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> referredPdDetailsPage = otherServicesTablesNativeQueryRepository.getListOfAllTheReferredPdsEOL(referrerPdUserId, startDate, endDate, searchString, pageable);

        List<ReferredPDDetailsRecord> referredPdDetailsRecords = new ArrayList<>();
        for (Object[] referredPdObj : referredPdDetailsPage.getContent()) {
            ReferredPDDetailsRecord referredPDDetailsRecord = ReferredPDDetailsRecord.fromObjectArray(referredPdObj);
            referredPdDetailsRecords.add(referredPDDetailsRecord);
        }

        return new PageImpl<>(referredPdDetailsRecords, pageable, referredPdDetailsPage.getTotalElements());
    }

    @Transactional
    public Page<ReferrerPDDetailsRecord> listReferrerPdDetails(String referredPdUserId, LocalDate startDate, LocalDate endDate, String searchString, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> referredPdDetailsPage = otherServicesTablesNativeQueryRepository.getListOfAllTheReferrerPds(referredPdUserId, startDate, endDate, searchString, pageable);

        List<ReferrerPDDetailsRecord> referrerPdDetailsRecords = new ArrayList<>();
        for (Object[] referredPdObj : referredPdDetailsPage.getContent()) {
            ReferrerPDDetailsRecord referrerPDDetailsRecord = ReferrerPDDetailsRecord.fromObjectArray(referredPdObj);

            //get the sum of treesAndLeafsEarned by the referredPDs
            BigDecimal treesEarnedByReferredPDs = otherServicesTablesNativeQueryRepository.getTotalTreesEarnedByReferredPdUsers(referrerPDDetailsRecord.getReferrerPdUserId());
            BigDecimal leafsEarnedByReferredPDs = otherServicesTablesNativeQueryRepository.getTotalLeafsEarnedByReferredPdUsers(referrerPDDetailsRecord.getReferrerPdUserId());
            referrerPDDetailsRecord.setSumOfTreesEarnedByReferredPDs(treesEarnedByReferredPDs);
            referrerPDDetailsRecord.setSumOfLeafsEarnedByReferredPDs(leafsEarnedByReferredPDs);

            referrerPdDetailsRecords.add(referrerPDDetailsRecord);
        }

        return new PageImpl<>(referrerPdDetailsRecords, pageable, referredPdDetailsPage.getTotalElements());
    }

    @Transactional
    public Page<ReferredPDDetailsRecord> listReferredPdDetails(String referrerPdUserId, String searchString, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> referredPdDetailsPage = otherServicesTablesNativeQueryRepository.getListOfAllTheReferredPds(referrerPdUserId, searchString, pageable);

        List<ReferredPDDetailsRecord> referredPDDetailsRecords = new ArrayList<>();
        for (Object[] referredPdObj : referredPdDetailsPage.getContent()) {
            ReferredPDDetailsRecord referredPDDetailsRecord = ReferredPDDetailsRecord.fromObjectArray(referredPdObj);
            referredPDDetailsRecords.add(referredPDDetailsRecord);
        }

        return new PageImpl<>(referredPDDetailsRecords, pageable, referredPdDetailsPage.getTotalElements());
    }

    @Transactional
    public Page<ReferralCommissionDetailsDTO> getReferralCommissionDetailsWithFilters(String referrerPdUserId,
                                                                                      int page,
                                                                                      int size,
                                                                                      String searchString,
                                                                                      LocalDate startDate,
                                                                                      LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> referralCommissionDetailsWithFiltersPage = otherServicesTablesNativeQueryRepository.getReferralCommissionDetailsWithFilters(
                referrerPdUserId,
                pageable,
                searchString,
                startDate,
                endDate);

        List<ReferralCommissionDetailsDTO> referralCommissionDetailsDTOList = new ArrayList<>();
        for (Object[] referralCommissionDetails : referralCommissionDetailsWithFiltersPage.getContent()) {
            ReferralCommissionDetailsDTO referralCommissionDetailsDTO = ReferralCommissionDetailsDTO.fromObjectArray(referralCommissionDetails);
            referralCommissionDetailsDTOList.add(referralCommissionDetailsDTO);
        }

        return new PageImpl<>(referralCommissionDetailsDTOList, pageable, referralCommissionDetailsWithFiltersPage.getTotalElements());
    }


    // This will be used later, when we will set up auto payment of commission
    @Transactional
//    public void giveCommissionToReferrer(Withdrawal withdrawal) throws Exception {
//        ReferralInfoDTO referralInfoDTO = getReferralInfo(withdrawal.getPdUserId());
//        if (referralInfoDTO != null) {
//            UserInfoDTO referrerPdUserInfoDTO = getUserInfo(referralInfoDTO.getReferrerPdUserId());
//            UserInfoDTO referredPdUserInfoDTO = getUserInfo(withdrawal.getPdUserId());
//            String commissionAmountInTrees = getCommissionAmountInTrees(withdrawal.getTrees(), referrerPdUserInfoDTO.getCommissionPercent());
//            BigDecimal commissionAmountInCents = new BigDecimal(commissionAmountInTrees).multiply(new BigDecimal(Long.toString(valueOfOneTreeInCents)));
//
//            String transferId = "";
//            String transferAmount = "0";
//            String transferDescription;
//            CommissionTransferStatus commissionTransferStatus;
//
//            if (referrerPdUserInfoDTO.getLinkedStripeId().isEmpty()) {
//                commissionTransferStatus = CommissionTransferStatus.TRANSFER_PENDING;
//                transferDescription = "Stripe ID is not set for referrer.";
//            } else {
//                PayReferrerThroughStripeResponse paymentResponse = payReferrerThroughStripe(referrerPdUserInfoDTO, referredPdUserInfoDTO, withdrawal, commissionAmountInCents);
//                if (paymentResponse.getException() != null) {
//                    commissionTransferStatus = CommissionTransferStatus.TRANSFER_FAILED;
//                    transferDescription = "Transfer failed: " + paymentResponse.getException().getMessage();
//                } else {
//                    commissionTransferStatus = CommissionTransferStatus.TRANSFER_COMPLETED;
//                    Transfer transfer = paymentResponse.getTransfer();
//                    transferId = transfer.getId();
//                    transferAmount = transfer.getAmount().toString();
//                    transferDescription = transfer.getDescription();
//                }
//            }
//
//            ReferralCommission referralCommission = createReferralCommission(withdrawal,
//                    referralInfoDTO, referrerPdUserInfoDTO,
//                    commissionAmountInTrees,
//                    commissionTransferStatus,
//                    transferId,
//                    new BigDecimal(transferAmount),
//                    transferDescription);
//            referralCommissionRepository.save(referralCommission);
//        }
//    }


    private ReferralInfoDTO getReferralInfo(String referredPdUserId) throws Exception {
        List<Object[]> referralInfo = otherServicesTablesNativeQueryRepository.findReferralDetailsByReferredPdUserId(referredPdUserId);

        if (referralInfo.isEmpty()) {
            return null;
        }

        if (referralInfo.size() > 1) {
            throw new Exception("This user was referred by multiple users, This is a server error");
        }

        Object[] obj = referralInfo.get(0);
        ReferralInfoDTO referralInfoDTO = new ReferralInfoDTO();
        referralInfoDTO.setId(obj[0].toString());
        referralInfoDTO.setReferredPdUserId(obj[1].toString());
        referralInfoDTO.setReferrerPdUserId(obj[2].toString());

        return referralInfoDTO;
    }

    private UserInfoDTO getUserInfo(String userId) throws Exception {
        List<Object[]> userInfo = otherServicesTablesNativeQueryRepository.findUserInfoByUserId(userId);
        if (userInfo.isEmpty()) {
            return null;
        }

        if (userInfo.size() > 1) {
            throw new Exception("Multiple users found for this userId, This is a server error");
        }

        String pdType = "GENERAL";
        String linkedStripeId = "";


        Object[] obj = userInfo.get(0);

        if (!obj[2].toString().isEmpty()) {
            pdType = obj[2].toString();
        }

        if (!obj[4].toString().isEmpty()) {
            linkedStripeId = obj[4].toString();
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(obj[0].toString());
        userInfoDTO.setEmail(obj[1].toString());
        userInfoDTO.setReferralGrade(pdType);
        userInfoDTO.setNickname(obj[3].toString());
        userInfoDTO.setLinkedStripeId(linkedStripeId);

        //Set commission rate
        if (userInfoDTO.getReferralGrade() != null) {
            if (userInfoDTO.getReferralGrade().equals("GENERAL")) {
                userInfoDTO.setCommissionPercent("1");
            } else if (userInfoDTO.getReferralGrade().equals("BEST")) {
                userInfoDTO.setCommissionPercent("3");
            } else if (userInfoDTO.getReferralGrade().equals("PARTNER")) {
                userInfoDTO.setCommissionPercent("5");
            } else if (userInfoDTO.getReferralGrade().equals("ENTERTAINMENT")) {
                userInfoDTO.setCommissionPercent("10");
            }
        }

        return userInfoDTO;
    }

    private PayReferrerThroughStripeResponse payReferrerThroughStripe(UserInfoDTO referrerPdUserInfoDTO, UserInfoDTO referredPdUserInfoDTO, Withdrawal withdrawal, BigDecimal commissionAmount) {
        try {
            Map<String, String> metaData = createMetaData(referrerPdUserInfoDTO, referredPdUserInfoDTO, withdrawal, commissionAmount);
            Transfer transfer = stripeClient.transferPayment(referrerPdUserInfoDTO.getLinkedStripeId(), referrerPdUserInfoDTO.getEmail(), commissionAmount.longValue(), metaData);
            return new PayReferrerThroughStripeResponse(null, transfer);
        } catch (Exception e) {
            String errorDetails = "Error while sending payment to the referrerPdUserId: " + referrerPdUserInfoDTO.getId() +
                    ", WithDrawalId: " + withdrawal.getId() + ", Error: " + e.getMessage();
            pdLogger.logException(GIVE_REFERRAL_COMMISSION, new Exception(errorDetails));
            return new PayReferrerThroughStripeResponse(e, null);
        }
    }

    private ReferralCommission createReferralCommission(Withdrawal withdrawal,
                                                        ReferralInfoDTO referralInfoDTO,
                                                        UserInfoDTO referrerPdUserInfoDTO,
                                                        String commissionAmountInTrees,
                                                        String commissionAmountInLeafs,
                                                        BigDecimal commissionAmountInCents
    ) {
        LocalDateTime createUpdateDate = LocalDateTime.now();
        return new ReferralCommission(
                withdrawal.getId(),
                referralInfoDTO.getReferrerPdUserId(),
                referrerPdUserInfoDTO.getCommissionPercent(),
                commissionAmountInTrees,
                commissionAmountInLeafs,
                createUpdateDate,
                createUpdateDate,
                CommissionTransferStatus.TRANSFER_PENDING,
                commissionAmountInCents,
                referrerPdUserInfoDTO.getReferralGrade()
        );
    }

    private Map<String, String> createMetaData(UserInfoDTO referrerPdUserInfoDTO, UserInfoDTO referredPdUserInfoDTO, Withdrawal withdrawal, BigDecimal commissionAmount) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put("Referrer_PdUserId", referrerPdUserInfoDTO.getId());
        metaData.put("Referrer_Email", referrerPdUserInfoDTO.getEmail());
        metaData.put("Referrer_PdType", referrerPdUserInfoDTO.getReferralGrade());
        metaData.put("Referrer_CommissionAmount", commissionAmount.toString());

        metaData.put("Referred_PdUserId", referredPdUserInfoDTO.getId());
        metaData.put("Referred_PdUser_Email", referredPdUserInfoDTO.getEmail());
        metaData.put("Referred_PdUser_PdType", referredPdUserInfoDTO.getReferralGrade());
        metaData.put("Referred_PdUser_WithDrawalId", withdrawal.getId());
        return metaData;
    }

    private String getCommissionAmountInTrees(BigDecimal trees, String percent) {
        if (trees.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal(0).toString();
        }
        BigDecimal percentage = new BigDecimal(percent);
        BigDecimal result = trees.multiply(percentage).divide(new BigDecimal(100));
        return result.toString();
    }

    private String getCommissionAmountInLeafs(BigDecimal leafs, String percent) {
        if (leafs.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal(0).toString();
        }
        BigDecimal percentage = new BigDecimal(percent);
        BigDecimal result = leafs.multiply(percentage).divide(new BigDecimal(100));
        return result.toString();
    }
}
