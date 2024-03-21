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

    @Autowired
    WalletService walletService;

    @Autowired
    WalletHistoryService walletHistoryService;

    @Autowired
    LedgerService ledgerService;

    public ResponseEntity<?> balanceTrees() {
        try {
            BigDecimal treeBalanceOfAllUsersCombined = walletRepository.sumOfAllTreesForUser();
            BigDecimal treeBalanceOfAllPDCombined = earningRepository.sumOfAllTreesEarned();
            return ResponseEntity.ok().body(new TreeBalanceResponse(null, treeBalanceOfAllUsersCombined, treeBalanceOfAllPDCombined));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TreeBalanceResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null, null));
        }
    }

    @Transactional
    public String addTreesFromBackend(String userId, BigDecimal purchasedTrees) throws Exception {
        try {
            Optional<Wallet> walletOptional = walletService.fetchWalletByUserId(userId);
            if (walletOptional.isPresent()) {
                Wallet wallet = walletService.updateWalletForUser(userId, purchasedTrees, new BigDecimal(0), LocalDateTime.now());

                UUID uuid = UUID.randomUUID();
                walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, purchasedTrees, new BigDecimal(0), LocalDateTime.now(), uuid.toString(), TransactionType.ADD_TREES_FROM_BACKEND.getDisplayName(),
                        new BigDecimal(0), "", "", "Added trees for the user using Admin dashbaord", "");

                ledgerService.saveToLedger(wallet.getId(), purchasedTrees, new BigDecimal(0), TransactionType.ADD_TREES_FROM_BACKEND);

                return "Successfully added trees for the user";
            } else {
                return "No wallet found for userId " + userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Payment Details update failed with following error : " + e.getMessage());
        }
    }

    @Transactional
    public String removeTreesFromBackend(String userId, BigDecimal trees) {
        Optional<Wallet> walletOptional = walletService.fetchWalletByUserId(userId);
        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            walletService.deductTreesFromWallet(userId, trees);
            UUID uuid = UUID.randomUUID();
            walletHistoryService.createWalletHistoryEntry(wallet.getId(), userId, trees, new BigDecimal(0), LocalDateTime.now(), uuid.toString(), TransactionType.REMOVE_TREES_FROM_BACKEND.getDisplayName(),
                    new BigDecimal(0), "", "", "Removed trees for the user using Admin dashbaord", "");
            ledgerService.saveToLedger(wallet.getId(), trees, new BigDecimal(0), TransactionType.REMOVE_TREES_FROM_BACKEND);
            return "Successfully removed trees for the user";
        } else {
            return "No wallet found for userId " + userId;
        }

    }

}
