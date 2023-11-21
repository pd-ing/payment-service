package com.pding.paymentservice.service;

import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.VideoTransactions;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.payload.response.BuyVideoResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GetVideoTransactionsResponse;
import com.pding.paymentservice.payload.response.IsVideoPurchasedByUserResponse;
import com.pding.paymentservice.payload.response.TotalTreesEarnedResponse;
import com.pding.paymentservice.repository.VideoTransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public VideoTransactions createVideoTransaction(String userId, String videoId, BigDecimal treesToConsumed, String videoOwnerUserId) {
        walletService.deductFromWallet(userId, treesToConsumed);

        VideoTransactions transaction = new VideoTransactions(userId, videoId, treesToConsumed, videoOwnerUserId);
        VideoTransactions video = videoTransactionsRepository.save(transaction);

        return video;
    }


    public List<VideoTransactions> getAllTransactionsForUser(String userID) {
        return videoTransactionsRepository.getVideoTransactionsByUserId(userID);
    }

    public BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserID) {
        return videoTransactionsRepository.getTotalTreesEarnedByVideoOwner(videoOwnerUserID);
    }

    public Boolean isVideoPurchasedByUser(String userID, String videoID) {
        List<VideoTransactions> videoTransactions = videoTransactionsRepository.findByUserIdAndVideoId(userID, videoID);
        if (videoTransactions == null)
            return false;

        if (videoTransactions.isEmpty())
            return false;

        return true;
    }

    public ResponseEntity<?> buyVideo(String userId, String videoId, BigDecimal trees, String videoOwnerUserId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        if (videoId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "videoid parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        try {
            VideoTransactions video = createVideoTransaction(userId, videoId, trees, videoOwnerUserId);
            return ResponseEntity.ok().body(new BuyVideoResponse(null, video));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientTreesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getVideoTransactions(String userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            List<VideoTransactions> videoTransactions = getAllTransactionsForUser(userId);
            return ResponseEntity.ok().body(new GetVideoTransactionsResponse(null, videoTransactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetVideoTransactionsResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getTreesEarned(String videoOwnerUserId) {
        if (videoOwnerUserId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "videoOwnerUserID parameter is required."));
        }
        try {
            BigDecimal videoTransactions = getTotalTreesEarnedByVideoOwner(videoOwnerUserId);
            return ResponseEntity.ok().body(new TotalTreesEarnedResponse(null, videoTransactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TotalTreesEarnedResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> isVideoPurchased(String userId, String videoId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new IsVideoPurchasedByUserResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."), false));
        }
        if (videoId == null) {
            return ResponseEntity.badRequest().body(new IsVideoPurchasedByUserResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "video parameter is required."), false));
        }
        try {
            Boolean isPurchased = isVideoPurchasedByUser(userId, videoId);
            return ResponseEntity.ok().body(new IsVideoPurchasedByUserResponse(null, isPurchased));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new IsVideoPurchasedByUserResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), false));
        }
    }
}
