package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.ReferralCommission;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.CommissionPaymentStatus;
import com.pding.paymentservice.models.other.services.tables.dto.ReferralInfoDTO;
import com.pding.paymentservice.models.other.services.tables.dto.UserInfoDTO;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.repository.ReferralCommissionRepository;
import com.pding.paymentservice.stripe.StripeClient;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

        //If PD is referred by someone then pay commission to the referrer
        if (referralInfoDTO != null) {
            //Get the details of the referrer
            UserInfoDTO userInfoDTO = getUserInfo(referralInfoDTO.getReferrerPdUserId());

            String commissionAmountInTrees = getPercentOfTrees(withdrawal.getTrees(), userInfoDTO.getCommissionPercent());

            LocalDateTime createUpdateDate = LocalDateTime.now();

            PaymentIntent paymentIntent = payReferrerThroughStripe(userInfoDTO, withdrawal);
            String paymentTransactionId = null;
            BigDecimal paymentAmount = null;
            LocalDateTime paymentDate = LocalDateTime.now();
            String stripePaymentStatus = null;
            String stripePaymentMethod = null;
            CommissionPaymentStatus commissionPaymentStatus = CommissionPaymentStatus.PENDING;
            if (paymentIntent == null) {
                commissionPaymentStatus = CommissionPaymentStatus.FAILED;
            } else {
                paymentTransactionId = paymentIntent.getId();
                paymentAmount = new BigDecimal(paymentIntent.getAmount().toString());
                stripePaymentStatus = paymentIntent.getStatus();
                stripePaymentMethod = paymentIntent.getPaymentMethod();
                commissionPaymentStatus = CommissionPaymentStatus.PAID;
            }

            ReferralCommission referralCommission = new ReferralCommission(
                    withdrawal.getId(),
                    referralInfoDTO.getReferrerPdUserId(),
                    referralInfoDTO.getReferredPdUserId(),
                    userInfoDTO.getCommissionPercent(),
                    commissionAmountInTrees,
                    createUpdateDate,
                    createUpdateDate,
                    commissionPaymentStatus,
                    paymentTransactionId,
                    paymentAmount,
                    paymentDate,
                    stripePaymentStatus,
                    stripePaymentMethod
            );
            referralCommissionRepository.save(referralCommission);
        }
    }

    public PaymentIntent payReferrerThroughStripe(UserInfoDTO userInfoDTO, Withdrawal withdrawal) {
        try {
            return stripeClient.sendPayment(userInfoDTO.getLinkedStripeId(), userInfoDTO.getEmail(), withdrawal.getTrees().longValue());
        } catch (Exception e) {
            Exception newException = new Exception("Error while sending payment to the referrerPdUserId:" + userInfoDTO.getId() +
                    "WithDrawalId:" + withdrawal.getId() + " , Error : " + e.getMessage());
            pdLogger.logException(GIVE_REFERRAL_COMMISSION, newException);
            return null;
        }
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
        referralInfoDTO.setReferrerPdUserId(obj[1].toString());
        referralInfoDTO.setReferredPdUserId(obj[2].toString());

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

        Object[] obj = userInfo.get(0);
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(obj[0].toString());
        userInfoDTO.setEmail(obj[1].toString());
        userInfoDTO.setPdType(obj[2].toString());
        userInfoDTO.setNickname(obj[3].toString());
        userInfoDTO.setLinkedStripeId(obj[4].toString());

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

    String getPercentOfTrees(BigDecimal trees, String percent) {
        BigDecimal percentage = new BigDecimal(percent);

        BigDecimal result = trees.multiply(percentage).divide(new BigDecimal(100));

        return result.toString();
    }

}
