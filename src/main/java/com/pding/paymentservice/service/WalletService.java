package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientLeafsException;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.WalletResponse;
import com.pding.paymentservice.repository.WalletRepository;
import com.pding.paymentservice.util.FirebaseRealtimeDbHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class WalletService {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    EarningService earningService;

    @Autowired
    FirebaseRealtimeDbHelper firebaseRealtimeDbHelper;


    @Autowired
    PdLogger pdLogger;

    @Transactional
    public Wallet addToWallet(String userId, BigDecimal trees, BigDecimal leafs, LocalDateTime lastPurchasedDate) {
        Optional<Wallet> optionalWallet = fetchWalletByUserId(userId);

        Wallet wallet;
        if (optionalWallet.isPresent()) {
            wallet = optionalWallet.get();

            BigDecimal updatedTrees = wallet.getTrees().add(trees);
            BigDecimal updatedLeafs = wallet.getLeafs().add(leafs);

            wallet.setTrees(updatedTrees);
            wallet.setLeafs(updatedLeafs);
            wallet.setLastPurchasedDate(LocalDateTime.now());
            wallet.setUpdatedDate(LocalDateTime.now());

            Integer updatedTotaltransactions = wallet.getTotalTransactions() + 1;
            wallet.setTotalTransactions(updatedTotaltransactions);

            walletRepository.save(wallet);
        } else {
            wallet = new Wallet(userId, trees, leafs, lastPurchasedDate);
            walletRepository.save(wallet);
        }
        firebaseRealtimeDbHelper.updateWalletBalanceInFirebase(userId, wallet.getLeafs(), wallet.getTrees());
        return wallet;
    }

    public Optional<Wallet> fetchWalletByUserId(String userId) {
        Optional<Wallet> wallet = walletRepository.findWalletByUserId(userId);

        if (!wallet.isPresent()) {
            Wallet newWallet = new Wallet();
            newWallet.setUserId(userId);
            newWallet.setTrees(new BigDecimal(0));
            newWallet.setLeafs(new BigDecimal(0));
            newWallet.setLastPurchasedDate(null);
            newWallet.setTotalTransactions(0);

            walletRepository.save(newWallet);
            firebaseRealtimeDbHelper.updateWalletBalanceInFirebase(userId, newWallet.getLeafs(), newWallet.getTrees());
            return walletRepository.findWalletByUserId(userId);
        }
        return wallet;
    }

    public Boolean canUserBuyVideo(String userId, BigDecimal treeToConsume) {
        Optional<Wallet> wallet = walletRepository.findWalletByUserId(userId);
        if (wallet.isPresent()) {
            Wallet walletObj = wallet.get();
            if (walletObj.getTrees().compareTo(treeToConsume) > 0) {
                return true;
            }
        }
        return false;
    }

    public void deductTreesFromWallet(String userId, BigDecimal treesToDeduct) {
        Optional<Wallet> wallet = fetchWalletByUserId(userId);

        if (wallet.isPresent()) {
            Wallet walletObj = wallet.get();
            BigDecimal currentTrees = walletObj.getTrees();

            if (treesToDeduct.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal newTreesBalance = currentTrees.subtract(treesToDeduct);
                if (newTreesBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    walletObj.setTrees(newTreesBalance);
                    walletRepository.save(walletObj);
                    firebaseRealtimeDbHelper.updateWalletBalanceInFirebase(userId, walletObj.getLeafs(), walletObj.getTrees());
                    pdLogger.logInfo("BUY_VIDEO", "Deducted " + treesToDeduct + " trees  for userId : " + userId);
                } else {
                    log.error("Insufficient trees. Cannot perform transaction.");
                    throw new InsufficientTreesException("Insufficient trees. Cannot perform transaction.");
                }
            } else {
                log.error("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
                throw new InvalidAmountException("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
            }
        } else {
            log.error("No wallet info present for userId " + userId);
            log.error("Creating wallet for for userId " + userId);
            //throw new WalletNotFoundException("No wallet info present for userID " + userId);
            throw new InsufficientTreesException("Insufficient trees. Cannot perform transaction.");
        }
    }

    public void deductLeafsFromWallet(String userId, BigDecimal leafsToDeduct) {
        pdLogger.logInfo("GIFT_TEST", "DdeductLeafsFromWallet for  UserId: " + userId + " , leafsToDeduct : " + leafsToDeduct);

        Optional<Wallet> wallet = fetchWalletByUserId(userId);

        if (wallet.isPresent()) {
            Wallet walletObj = wallet.get();
            BigDecimal currentLeafs = walletObj.getLeafs();

            if (leafsToDeduct.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal newLeafBalance = currentLeafs.subtract(leafsToDeduct);
                if (newLeafBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    walletObj.setLeafs(newLeafBalance);
                    walletRepository.save(walletObj);
                    firebaseRealtimeDbHelper.updateWalletBalanceInFirebase(userId, walletObj.getLeafs(), walletObj.getTrees());
                } else {
                    log.error("Insufficient leafs. Cannot perform transaction.");
                    throw new InsufficientLeafsException("Insufficient leafs. Cannot perform transaction.");
                }
            } else {
                log.error("Invalid amount(leafs) to deduct. Amount(leafs) must be greater than or equal to zero.");
                throw new InvalidAmountException("Invalid amount(leafs) to deduct. Amount(leafs) must be greater than or equal to zero.");
            }
        } else {
            log.error("No wallet info present for userId " + userId);
            log.error("Creating wallet for for userId " + userId);
            //throw new WalletNotFoundException("No wallet info present for userID " + userId);
            throw new InsufficientLeafsException("Insufficient leafs. Cannot perform transaction.");
        }
    }

    public Wallet updateWalletForUser(String userId, BigDecimal purchasedTrees, BigDecimal purchasedLeafs, LocalDateTime purchasedDate) {
        Wallet wallet = addToWallet(userId, purchasedTrees, purchasedLeafs, purchasedDate);
        log.info("Wallet table updated", wallet);
        return wallet;
    }

    public ResponseEntity<?> getWallet(String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            Optional<Wallet> wallet = fetchWalletByUserId(userId);
            Optional<Earning> earning = earningService.fetchEarningForUserId(userId);
            return ResponseEntity.ok().body(new WalletResponse(null, wallet.get(), earning.get()));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WALLET, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WalletResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null, null));
        }
    }

}
