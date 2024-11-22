package com.pding.paymentservice.service;

import com.pding.paymentservice.models.Ledger;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.repository.LedgerRespository;
import com.pding.paymentservice.util.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class LedgerService {

    @Autowired
    LedgerRespository ledgerRespository;

    public void saveToLedger(String referenceId, BigDecimal treesTransacted, BigDecimal leafsTransacted, TransactionType transactionType, String userId) {
        Ledger ledger = new Ledger(referenceId, treesTransacted, leafsTransacted, transactionType, userId);
        ledgerRespository.save(ledger);
        log.info("Ledger saved for referenceId: {}, treesTransacted: {}, leafsTransacted: {}, transactionType: {}, userId: {}",
            LogSanitizer.sanitizeForLog(referenceId), LogSanitizer.sanitizeForLog(treesTransacted), LogSanitizer.sanitizeForLog(leafsTransacted), LogSanitizer.sanitizeForLog(transactionType), LogSanitizer.sanitizeForLog(userId));
    }
}

