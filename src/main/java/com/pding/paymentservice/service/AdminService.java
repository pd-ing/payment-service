package com.pding.paymentservice.service;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.admin.TreeBalanceResponse;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
            BigDecimal leafBalanceOfAllUsersCombined = walletRepository.sumOfAllLeafsForUser();
            BigDecimal leafBalanceOfAllPDCombined = earningRepository.sumOfAllLeafsEarned();
            return ResponseEntity.ok().body(new TreeBalanceResponse(null, treeBalanceOfAllUsersCombined, treeBalanceOfAllPDCombined, leafBalanceOfAllUsersCombined, leafBalanceOfAllPDCombined));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TreeBalanceResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null, null, null, null));
        }
    }
}
