package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GenericListDataResponse;
import com.pding.paymentservice.payload.response.GenericStringResponse;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.repository.WithdrawalRepository;
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


    @Transactional
    public void startWithdrawal(String pdUserId, BigDecimal trees, String transactionId) throws Exception {
        if (LocalDateTime.now().getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new Exception("Withdrawal requests can only be made on Mondays.");
        }

        List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, WithdrawalStatus.PENDING);
        if (!withdrawalList.isEmpty()) {
            throw new Exception("You already have an ongoing withdrawal request.");
        }

        earningService.deductFromEarning(pdUserId, trees);

        Withdrawal withdrawal = new Withdrawal(pdUserId, trees, transactionId, WithdrawalStatus.PENDING);
        withdrawalRepository.save(withdrawal);

        ledgerService.saveToLedger(withdrawal.getId(), trees, TransactionType.WITHDRAWAL_STARTED);
    }


    @Transactional
    public void completeWithdrawal(String transactionId) {
        Optional<Withdrawal> withdrawalOptional = withdrawalRepository.findByTransactionId(transactionId);

        if (withdrawalOptional.isPresent()) {

            Withdrawal withdrawal = withdrawalOptional.get();
            withdrawal.setStatus(WithdrawalStatus.COMPLETE);

            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), TransactionType.WITHDRAWAL_COMPLETED);
        } else {
            log.info("No withdrawal found with transactionId " + transactionId);
        }
    }

    @Transactional
    public void failWithdrawal(String transactionId) {
        Optional<Withdrawal> withdrawalOptional = withdrawalRepository.findByTransactionId(transactionId);

        if (withdrawalOptional.isPresent()) {

            Withdrawal withdrawal = withdrawalOptional.get();
            withdrawal.setStatus(WithdrawalStatus.FAILED);

            earningService.addToEarning(withdrawal.getPdUserId(), withdrawal.getTrees());
            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), TransactionType.WITHDRAWAL_FAILED);
            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), TransactionType.TREES_REVERTED);
        } else {
            log.info("No withdrawal found with transactionId " + transactionId);
        }
    }

    public ResponseEntity<?> withDraw(String pdUserId, BigDecimal trees, String transactionId) {
        if (pdUserId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        if (transactionId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "transactionId parameter is required."));
        }

        try {
            startWithdrawal(pdUserId, trees, transactionId);
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Withdrwal process initialted successfully, Will take 5-7 businees days to credit in your account"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getWithDrawTransactions(String pdUserId) {
        if (pdUserId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }

        try {
            List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdOrderByCreatedDateDesc(pdUserId);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, withdrawalList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

}
