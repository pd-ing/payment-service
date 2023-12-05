package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.repository.WithdrawalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public void startWithdrawal(String pdUserId, BigDecimal trees, String transactionId) {
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

            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), TransactionType.WITHDRAWAL_FAILED);
        } else {
            log.info("No withdrawal found with transactionId " + transactionId);
        }
    }

}
