package com.pding.paymentservice.service;

import com.pding.paymentservice.models.Ledger;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.repository.LedgerRespository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class LedgerService {

    @Autowired
    LedgerRespository ledgerRespository;

    public void saveToLedger(String walletOrVideoOrDonationOrWithdrawalId, BigDecimal treesTransacted, TransactionType transactionType) {
        Ledger ledger = new Ledger(walletOrVideoOrDonationOrWithdrawalId, treesTransacted, transactionType);
        ledgerRespository.save(ledger);
    }
}

