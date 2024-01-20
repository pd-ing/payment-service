package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InvalidTransactionIDException;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import com.pding.paymentservice.payload.request.WithdrawRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GenericListDataResponse;
import com.pding.paymentservice.payload.response.GenericStringResponse;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.repository.WithdrawalRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.stripe.StripeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WithdrawalService {

    @Autowired
    WithdrawalRepository withdrawalRepository;

    @Autowired
    EarningService earningService;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    private StripeClient stripeClient;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    AuthHelper authHelper;


    @Transactional
    void startWithdrawal(String pdUserId, BigDecimal trees, BigDecimal leafs) throws Exception {
//        if (LocalDateTime.now().getDayOfWeek() != DayOfWeek.MONDAY) {
//            throw new Exception("Withdrawal requests can only be made on Mondays.");
//        }

        List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, WithdrawalStatus.PENDING);
        if (!withdrawalList.isEmpty()) {
            throw new Exception("You already have an ongoing withdrawal request.");
        }

        earningService.deductTreesAndLeafs(pdUserId, trees, leafs);

        Withdrawal withdrawal = new Withdrawal(pdUserId, trees, leafs, WithdrawalStatus.PENDING);
        withdrawalRepository.save(withdrawal);

        ledgerService.saveToLedger(withdrawal.getId(), trees, leafs, TransactionType.WITHDRAWAL_STARTED);
    }


    @Transactional
    void completeWithdrawal(String pdUserId) throws Exception {
        List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, WithdrawalStatus.PENDING);

        if (withdrawalList.size() == 1) {

            Withdrawal withdrawal = withdrawalList.get(0);
            withdrawal.setStatus(WithdrawalStatus.COMPLETE);

            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), withdrawal.getLeafs(), TransactionType.WITHDRAWAL_COMPLETED);
        } else if (withdrawalList.size() > 1) {
            throw new Exception("More than 1 withdrawal request found in PENDING status for pdUserId " + pdUserId);
        } else {
            throw new Exception("Invalid transaction, Please start withdraw request before completing it ");
        }
    }

    @Transactional
    void failWithdrawal(String pdUserId) throws Exception {
        List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, WithdrawalStatus.PENDING);

        if (withdrawalList.size() == 1) {

            Withdrawal withdrawal = withdrawalList.get(0);
            withdrawal.setStatus(WithdrawalStatus.FAILED);

            earningService.addTreesAndLeafsToEarning(withdrawal.getPdUserId(), withdrawal.getTrees(), withdrawal.getLeafs());

            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), withdrawal.getLeafs(), TransactionType.WITHDRAWAL_FAILED);
            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), new BigDecimal(0), TransactionType.TREES_REVERTED);
            ledgerService.saveToLedger(withdrawal.getId(), new BigDecimal(0), withdrawal.getLeafs(), TransactionType.LEAFS_REVERTED);
        } else if (withdrawalList.size() > 1) {
            throw new Exception("More than 1 withdrawal request found in PENDING status for pdUserId " + pdUserId);
        } else {
            throw new Exception("Invalid transaction, Please start withdraw request before failing it ");
        }
    }

    private boolean validatePaymentIntentID(String pdUserId, String transactionId) throws Exception {
        if (!stripeClient.isPaymentIntentIDPresentInStripe(transactionId)) {
            throw new InvalidTransactionIDException("paymentIntent id : " + transactionId + " , is invalid");
        }

//        if (withdrawalRepository.findByPdUserIdAndTransactionId(pdUserId, transactionId).isPresent()) {
//            throw new InvalidTransactionIDException("paymentIntent id : " + transactionId + " , is already used for the payment");
//        }

        return true;
    }

    public ResponseEntity<?> startWithDrawal(WithdrawRequest withdrawRequest) {
        if (withdrawRequest.getTrees() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        if (withdrawRequest.getLeafs() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafs parameter is required."));
        }

        try {
            String pdUserId = authHelper.getUserId();
            startWithdrawal(pdUserId, withdrawRequest.getTrees(), withdrawRequest.getLeafs());
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Withdrwal process initialted successfully, Will take 5-7 businees days to credit in your account"));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.START_WITHDRAW, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getAllWithDrawTransactions() {
        try {
            String pdUserId = authHelper.getUserId();
            List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdOrderByCreatedDateDesc(pdUserId);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, withdrawalList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getWithDrawTransactions(String status) {
        if (status == null || status.isEmpty()) {
            return getAllWithDrawTransactions();
        }
        WithdrawalStatus withdrawalStatus = null;
        if (status.equals("pending")) {
            withdrawalStatus = WithdrawalStatus.PENDING;
        } else if (status.equals("failed")) {
            withdrawalStatus = WithdrawalStatus.FAILED;
        } else if (status.equals("complete")) {
            withdrawalStatus = WithdrawalStatus.COMPLETE;
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid value for status parameter, Following are valid values pending, failed, complete"));
        }

        try {
            String pdUserId = authHelper.getUserId();
            List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, withdrawalStatus);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, withdrawalList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getPendingWithDrawTransactions() {
        try {
            String pdUserId = authHelper.getUserId();
            List<Withdrawal> withdrawalList = withdrawalRepository.findByStatus(WithdrawalStatus.PENDING);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, withdrawalList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> completeWithDraw(WithdrawRequest withdrawRequest) {
        if (withdrawRequest.getPdUserId() == null || withdrawRequest.getPdUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }

        try {
            completeWithdrawal(withdrawRequest.getPdUserId());
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Withdrwal process completed successfully, Will take 5-7 businees days to credit in your account"));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.COMPLETE_WITHDRAW, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> failWithDraw(WithdrawRequest withdrawRequest) {
        if (withdrawRequest.getPdUserId() == null || withdrawRequest.getPdUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }

        try {
            failWithdrawal(withdrawRequest.getPdUserId());
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Withdrawal Failed, Trees and Leafs rollback done successfully"));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.COMPLETE_WITHDRAW, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

}
