package com.pding.paymentservice.service;

import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.Trees;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional
    public Wallet addToWallet(long userID, BigDecimal trees, LocalDateTime lastPurchasedDate) {
        Optional<Wallet> optionalWallet = fetchWalletByUserID(userID);

        Wallet wallet;
        if (optionalWallet.isPresent()) {
            wallet = optionalWallet.get();

            BigDecimal updatedTree = wallet.getTrees().add(trees);

            wallet.setTrees(updatedTree);
            wallet.setLastPurchasedDate(LocalDateTime.now());
            wallet.setUpdatedDate(LocalDateTime.now());

            Integer updatedTotaltransactions = wallet.getTotalTransactions() + 1;
            wallet.setTotalTransactions(updatedTotaltransactions);

            walletRepository.save(wallet);

        } else {
            wallet = new Wallet(userID, trees, lastPurchasedDate);
            walletRepository.save(wallet);
        }
        return wallet;
    }

    public Optional<Wallet> fetchWalletByUserID(long userID) {
        Optional<Wallet> wallet = walletRepository.findWalletByUserID(userID);

        if (!wallet.isPresent()) {
            Wallet newWallet = new Wallet(userID, new BigDecimal(0), null);
            walletRepository.save(newWallet);

            return walletRepository.findWalletByUserID(userID);
        }
        return wallet;
    }

    public Boolean canUserBuyVideo(long userID, BigDecimal treeToConsume) {
        Optional<Wallet> wallet = walletRepository.findWalletByUserID(userID);
        if (wallet.isPresent()) {
            Wallet walletObj = wallet.get();
            if (walletObj.getTrees().compareTo(treeToConsume) > 0) {
                return true;
            }
        }
        return false;
    }

    public void deductFromWallet(long userID, BigDecimal treesToDeduct) {
        Optional<Wallet> wallet = fetchWalletByUserID(userID);

        if (wallet.isPresent()) {
            Wallet walletObj = wallet.get();
            BigDecimal currentTrees = walletObj.getTrees();

            if (treesToDeduct.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal newTreesBalance = currentTrees.subtract(treesToDeduct);
                if (newTreesBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    walletObj.setTrees(newTreesBalance);
                    walletRepository.save(walletObj);
                } else {
                    log.error("Insufficient trees. Cannot perform transaction.");
                    throw new InsufficientTreesException("Insufficient trees. Cannot perform transaction.");
                }
            } else {
                log.error("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
                throw new InvalidAmountException("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
            }
        } else {
            log.error("No wallet info present for userID " + userID);
            throw new WalletNotFoundException("No wallet info present for userID " + userID);
        }
    }
}
