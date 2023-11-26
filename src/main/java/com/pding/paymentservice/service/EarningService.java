package com.pding.paymentservice.service;

import com.pding.paymentservice.models.Earning;
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
    public void addToEarning(String userId, BigDecimal trees) {
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
            earningObj = new Earning(userId, trees);
        }
        earningRepository.save(earningObj);
    }

    public Optional<Earning> getEarningForUserId(String userId) {
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
}
