package com.pding.paymentservice.service;

import com.pding.paymentservice.exception.EarningNotFoundException;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.repository.EarningRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class EarningService {

    @Autowired
    EarningRepository earningRepository;

    @Transactional
    public void addTreesToEarning(String userId, BigDecimal trees) {
        Optional<Earning> earning = earningRepository.findByUserId(userId);

        Earning earningObj = null;
        if (earning.isPresent()) {
            earningObj = earning.get();

            BigDecimal updatedTreesEarned = earningObj.getTreesEarned().add(trees);

            earningObj.setTreesEarned(updatedTreesEarned);
            earningObj.setUpdatedDate(LocalDateTime.now());

            Integer updatedTotalTransactions = earningObj.getTotalTransactions() + 1;
            earningObj.setTotalTransactions(updatedTotalTransactions);
        } else {
            earningObj = new Earning(userId, trees, new BigDecimal(0));
        }
        earningRepository.save(earningObj);
    }


    @Transactional
    public void addLeafsToEarning(String userId, BigDecimal leafs) {
        Optional<Earning> earning = earningRepository.findByUserId(userId);

        Earning earningObj = null;
        if (earning.isPresent()) {
            earningObj = earning.get();

            BigDecimal updatedLeafsEarned = earningObj.getLeafsEarned().add(leafs);

            earningObj.setLeafsEarned(updatedLeafsEarned);
            earningObj.setUpdatedDate(LocalDateTime.now());

            Integer updatedTotalTransactions = earningObj.getTotalTransactions() + 1;
            earningObj.setTotalTransactions(updatedTotalTransactions);
        } else {
            earningObj = new Earning(userId, new BigDecimal(0), leafs);
        }
        earningRepository.save(earningObj);
    }


    public Optional<Earning> fetchEarningForUserId(String userId) {
        Optional<Earning> earning = earningRepository.findByUserId(userId);
        if (!earning.isPresent()) {
            Earning earningObj = new Earning();
            earningObj.setUserId(userId);
            earningObj.setTreesEarned(new BigDecimal(0));
            earningObj.setTotalTransactions(0);

            earningRepository.save(earningObj);

            return earningRepository.findByUserId(userId);
        }

        return earning;
    }


    public void deductFromEarning(String userId, BigDecimal treesToDeduct) {
        Optional<Earning> earning = fetchEarningForUserId(userId);

        if (earning.isPresent()) {
            Earning earningObj = earning.get();
            BigDecimal currentTrees = earningObj.getTreesEarned();

            if (treesToDeduct.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal newTreesBalance = currentTrees.subtract(treesToDeduct);
                if (newTreesBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    earningObj.setTreesEarned(newTreesBalance);
                    earningRepository.save(earningObj);
                } else {
                    log.error("Insufficient trees. Cannot perform transaction.");
                    throw new InsufficientTreesException("Insufficient trees. Cannot perform transaction.");
                }
            } else {
                log.error("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
                throw new InvalidAmountException("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
            }
        } else {
            log.error("No Earning info present for userId " + userId);
            log.error("Creating Earning info for for userId " + userId);
            throw new EarningNotFoundException("No Earnings found for userID " + userId);
        }
    }
}
