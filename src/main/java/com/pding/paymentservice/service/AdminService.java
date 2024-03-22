package com.pding.paymentservice.service;

import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GenericListDataResponse;
import com.pding.paymentservice.payload.response.admin.TreeBalanceResponse;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    EarningRepository earningRepository;

    public ResponseEntity<?> balanceTrees() {
        try {
            BigDecimal treeBalanceOfAllUsersCombined = walletRepository.sumOfAllTreesForUser();
            BigDecimal treeBalanceOfAllPDCombined = earningRepository.sumOfAllTreesEarned();
            return ResponseEntity.ok().body(new TreeBalanceResponse(null, treeBalanceOfAllUsersCombined, treeBalanceOfAllPDCombined));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TreeBalanceResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null, null));
        }
    }
}
