package com.pding.paymentservice.service;

import com.pding.paymentservice.models.VideoTransactions;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.repository.VideoTransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VideoTransactionsService {

    @Autowired
    VideoTransactionsRepository videoTransactionsRepository;

    @Autowired
    WalletService walletService;

    @Transactional
    public VideoTransactions createVideoTransaction(Long userID, Long contentID, BigDecimal treesToConsumed, Long videoOwnerUserID) {
        walletService.deductFromWallet(userID, treesToConsumed);

        VideoTransactions transaction = new VideoTransactions(userID, contentID, treesToConsumed, videoOwnerUserID);
        VideoTransactions video = videoTransactionsRepository.save(transaction);

        return video;
    }

    public List<VideoTransactions> getAllTransactionsForUser(long userID) {
        return videoTransactionsRepository.getVideoTransactionsByUserID(userID);
    }

    public BigDecimal getTotalTreesEarnedByVideoOwner(Long videoOwnerUserID) {
        return videoTransactionsRepository.getTotalTreesEarnedByVideoOwner(videoOwnerUserID);
    }
}
