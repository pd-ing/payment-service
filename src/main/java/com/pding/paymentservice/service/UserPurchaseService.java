package com.pding.paymentservice.service;

import com.pding.paymentservice.repository.DonationRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class UserPurchaseService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private VideoPurchaseRepository videoPurchaseRepository;

    public List<String> findUserPurchaseFromLastDays(int days) {
        log.info("Finding users who have purchase in the last {} days", days);
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime fromDateTime = currentDateTime.minusDays(days);

        CompletableFuture<List<String>> userPurchaseFromDateTimeFuture = CompletableFuture.supplyAsync(() -> videoPurchaseRepository.findUsersPurchaseFromDateTime(fromDateTime));
        CompletableFuture<List<String>> userPurchaseFromLastUpdateFuture = CompletableFuture.supplyAsync(() -> donationRepository.findUsersPurchaseFromLastUpdate(fromDateTime));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(userPurchaseFromDateTimeFuture, userPurchaseFromLastUpdateFuture);
        List<String> uniqueUserIds = new ArrayList<>();
        try {
            allFutures.get();
            Set<String> uniqueUserIdSet = new HashSet<>();
            uniqueUserIdSet.addAll(userPurchaseFromDateTimeFuture.get());
            uniqueUserIdSet.addAll(userPurchaseFromLastUpdateFuture.get());
            uniqueUserIds = new ArrayList<>(uniqueUserIdSet);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while fetching user purchase data", e);
            Thread.currentThread().interrupt();
        }
        return uniqueUserIds;
    }
}
