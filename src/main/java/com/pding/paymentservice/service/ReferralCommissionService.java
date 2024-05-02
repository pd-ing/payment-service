package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.ReferralCommission;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.CommissionTransferStatus;
import com.pding.paymentservice.models.other.services.tables.dto.ReferralInfoDTO;
import com.pding.paymentservice.models.other.services.tables.dto.UserInfoDTO;
import com.pding.paymentservice.payload.response.PayReferrerThroughStripeResponse;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.repository.ReferralCommissionRepository;
import com.pding.paymentservice.stripe.StripeClient;
import com.stripe.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pding.paymentservice.PdLogger.EVENT.GIVE_REFERRAL_COMMISSION;

@Service
public class ReferralCommissionService {
    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    @Autowired
    ReferralCommissionRepository referralCommissionRepository;

    @Autowired
    StripeClient stripeClient;

    @Autowired
    PdLogger pdLogger;

    @Transactional
    public void giveCommissionToReferrer(Withdrawal withdrawal) throws Exception {
        ReferralInfoDTO referralInfoDTO = getReferralInfo(withdrawal.getPdUserId());

        if (referralInfoDTO != null) {
            UserInfoDTO referrerPdUserInfoDTO = getUserInfo(referralInfoDTO.getReferrerPdUserId());
            UserInfoDTO referredPdUserInfoDTO = getUserInfo(withdrawal.getPdUserId());
            String commissionAmountInTrees = getCommissionAmountInTrees(withdrawal.getTrees(), referrerPdUserInfoDTO.getCommissionPercent());

            String transferId = "";
            String transferAmount = "0";
            String transferDescription;
            CommissionTransferStatus commissionTransferStatus;

            if (referrerPdUserInfoDTO.getLinkedStripeId().isEmpty()) {
                commissionTransferStatus = CommissionTransferStatus.TRANSFER_PENDING;
                transferDescription = "Stripe ID is not set for referrer.";
            } else {
                PayReferrerThroughStripeResponse paymentResponse = payReferrerThroughStripe(referrerPdUserInfoDTO, referredPdUserInfoDTO, withdrawal, new BigDecimal(commissionAmountInTrees));
                if (paymentResponse.getException() != null) {
                    commissionTransferStatus = CommissionTransferStatus.TRANSFER_FAILED;
                    transferDescription = "Transfer failed: " + paymentResponse.getException().getMessage();
                } else {
                    commissionTransferStatus = CommissionTransferStatus.TRANSFER_DONE;
                    Transfer transfer = paymentResponse.getTransfer();
                    transferId = transfer.getId();
                    transferAmount = transfer.getAmount().toString();
                    transferDescription = transfer.getDescription();
                }
            }

            ReferralCommission referralCommission = createReferralCommission(withdrawal,
                    referralInfoDTO, referrerPdUserInfoDTO,
                    commissionAmountInTrees,
                    commissionTransferStatus,
                    transferId,
                    new BigDecimal(transferAmount),
                    transferDescription);
            referralCommissionRepository.save(referralCommission);
        }
    }

    public PayReferrerThroughStripeResponse payReferrerThroughStripe(UserInfoDTO referrerPdUserInfoDTO, UserInfoDTO referredPdUserInfoDTO, Withdrawal withdrawal, BigDecimal commissionAmount) {
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
                                                        CommissionTransferStatus commissionTransferStatus,
                                                        String transferId,
                                                        BigDecimal transferAmount,
                                                        String transferDescription) {
        LocalDateTime createUpdateDate = LocalDateTime.now();
        return new ReferralCommission(
                withdrawal.getId(),
                referralInfoDTO.getReferrerPdUserId(),
                referralInfoDTO.getReferredPdUserId(),
                referrerPdUserInfoDTO.getCommissionPercent(),
                commissionAmountInTrees,
                createUpdateDate,
                createUpdateDate,
                commissionTransferStatus,
                transferId,
                transferAmount,
                transferDescription
        );
    }

    private Map<String, String> createMetaData(UserInfoDTO referrerPdUserInfoDTO, UserInfoDTO referredPdUserInfoDTO, Withdrawal withdrawal, BigDecimal commissionAmount) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put("Referrer_PdUserId", referrerPdUserInfoDTO.getId());
        metaData.put("Referrer_Email", referrerPdUserInfoDTO.getEmail());
        metaData.put("Referrer_PdType", referrerPdUserInfoDTO.getPdType());
        metaData.put("Referrer_CommissionAmount", commissionAmount.toString());

        metaData.put("Referred_PdUserId", referredPdUserInfoDTO.getId());
        metaData.put("Referred_PdUser_Email", referredPdUserInfoDTO.getEmail());
        metaData.put("Referred_PdUser_PdType", referredPdUserInfoDTO.getPdType());
        metaData.put("Referred_PdUser_WithDrawalId", withdrawal.getId());
        return metaData;
    }

    public ReferralInfoDTO getReferralInfo(String referredPdUserId) throws Exception {
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

    public UserInfoDTO getUserInfo(String userId) throws Exception {
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
        userInfoDTO.setPdType(pdType);
        userInfoDTO.setNickname(obj[3].toString());
        userInfoDTO.setLinkedStripeId(linkedStripeId);

        //Set commission rate
        if (userInfoDTO.getPdType() != null) {
            if (userInfoDTO.getPdType().equals("GENERAL")) {
                userInfoDTO.setCommissionPercent("1");
            } else if (userInfoDTO.getPdType().equals("BEST")) {
                userInfoDTO.setCommissionPercent("3");
            } else if (userInfoDTO.getPdType().equals("PARTNER")) {
                userInfoDTO.setCommissionPercent("5");
            }
        }

        return userInfoDTO;
    }

    private String getCommissionAmountInTrees(BigDecimal trees, String percent) {
        BigDecimal percentage = new BigDecimal(percent);
        BigDecimal result = trees.multiply(percentage).divide(new BigDecimal(100));
        return result.toString();
    }

}
