package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.response.BuyVideoResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GetVideoTransactionsResponse;
import com.pding.paymentservice.payload.response.IsVideoPurchasedByUserResponse;
import com.pding.paymentservice.payload.response.TotalTreesEarnedResponse;
import com.pding.paymentservice.models.VideoEarningsAndSales;
import com.pding.paymentservice.payload.response.VideoEarningsAndSalesResponse;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class VideoPurchaseService {

    @Autowired
    VideoPurchaseRepository videoPurchaseRepository;

    @Autowired
    WalletService walletService;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    EarningService earningService;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    AuthHelper authHelper;


    @Transactional
    public VideoPurchase createVideoTransaction(String userId, String videoId, BigDecimal treesToConsumed, String videoOwnerUserId) {
        walletService.deductTreesFromWallet(userId, treesToConsumed);

        VideoPurchase transaction = new VideoPurchase(userId, videoId, treesToConsumed, videoOwnerUserId);
        VideoPurchase video = videoPurchaseRepository.save(transaction);
        pdLogger.logInfo("BUY_VIDEO", "Video purchase record created with details UserId : " + userId + " ,VideoId : " + videoId + ", trees : " + treesToConsumed + ", VideoOwnerUserId : " + videoOwnerUserId);

        earningService.addTreesToEarning(videoOwnerUserId, treesToConsumed);
        ledgerService.saveToLedger(video.getId(), treesToConsumed, new BigDecimal(0), TransactionType.VIDEO_PURCHASE);
        pdLogger.logInfo("BUY_VIDEO", "Video purchase details recorded in LEGDER VideoId : " + videoId + ", trees : " + treesToConsumed + ", TransactionType : " + TransactionType.VIDEO_PURCHASE);

        return video;
    }


    public List<VideoPurchase> getAllTransactionsForUser(String userID) {
        return videoPurchaseRepository.getVideoPurchaseByUserId(userID);
    }

    public BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserID) {
        return videoPurchaseRepository.getTotalTreesEarnedByVideoOwner(videoOwnerUserID);
    }

    public Boolean isVideoPurchasedByUser(String userID, String videoID) {
        List<VideoPurchase> videoTransactions = videoPurchaseRepository.findByUserIdAndVideoId(userID, videoID);
        if (videoTransactions == null)
            return false;

        if (videoTransactions.isEmpty())
            return false;

        return true;
    }

    boolean validateActualCostOfVideo(String videoId, String videoOwnerUserId, BigDecimal treesProvidedByUser) {
        BigDecimal actualVideoCostInTrees = videoPurchaseRepository.findActualCostOfVideo(videoId, videoOwnerUserId);

        if (actualVideoCostInTrees != null) {
            if (actualVideoCostInTrees.compareTo(treesProvidedByUser) == 0) {
                return true; //this is the case where treesProvidedByUser match with the actual cost of the video
            } else {
                pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, new Exception("treesProvidedByUser does not match with the actual cost of the video"));
                return false; //this is the case where treesProvidedByUser does not match with the actual cost of the video
            }
        } else {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, new Exception("Buy video request made for the video which is not present in database"));
            return false; // this is the case where video is not present in video table, Don't allow to buy it
        }
    }

    public Map<String, VideoEarningsAndSales> getVideoStats(List<String> videoId) {
        return videoPurchaseRepository.getTotalTreesEarnedAndSalesCountMapForVideoIds(videoId);
    }

    public ResponseEntity<?> buyVideo(String userId, String videoId, BigDecimal trees, String videoOwnerUserId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        if (videoId == null || videoId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "videoid parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        if (trees.equals(new BigDecimal(0))) {
            pdLogger.logInfo("BUY_VIDEO", "Attempt made to purchase the video for 0 trees by, userId : " + userId + " ,VideoId : " + videoId + ", trees : " + trees + ", VideoOwnerUserId : " + videoOwnerUserId);
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Video cannnot be purchased for 0 trees"));
        }
        if (videoOwnerUserId == null || videoOwnerUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "videoOwnerUserId parameter is required."));
        }
        if (!validateActualCostOfVideo(videoId, videoOwnerUserId, trees)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Actual cost of video does not match with the trees provided to buy it"));
        }

        try {
            pdLogger.logInfo("BUY_VIDEO", "Buy video request made with following details UserId : " + userId + " ,VideoId : " + videoId + ", trees : " + trees + ", VideoOwnerUserId : " + videoOwnerUserId);
            VideoPurchase video = createVideoTransaction(userId, videoId, trees, videoOwnerUserId);
            return ResponseEntity.ok().body(new BuyVideoResponse(null, video));
        } catch (WalletNotFoundException e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientTreesException e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    public ResponseEntity<?> buyVideoV2(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "videoid parameter is required."));
        }

        try {
            String userId = authHelper.getUserId();
            List<Object[]> rawResults = videoPurchaseRepository.findUserIdAndTreesByVideoId(videoId);
            String videoOwnerUserId = "";
            BigDecimal trees = null;

            for (Object[] row : rawResults) {
                videoOwnerUserId = (String) row[0];
                trees = (BigDecimal) row[1];
            }

            if (trees == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "trees is null for the videoId provided"), null));
            }

            if (videoOwnerUserId == null || videoOwnerUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), " VideoOwnerUserID is null or empty for the videoId provided"), null));
            }

            pdLogger.logInfo("BUY_VIDEO", "Buy video request made with following details UserId : " + userId + " ,VideoId : " + videoId + ", trees : " + trees + ", VideoOwnerUserId : " + videoOwnerUserId);

            VideoPurchase video = createVideoTransaction(userId, videoId, trees, videoOwnerUserId);
            return ResponseEntity.ok().body(new BuyVideoResponse(null, video));
        } catch (WalletNotFoundException e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientTreesException e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getVideoTransactions(String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            List<VideoPurchase> videoTransactions = getAllTransactionsForUser(userId);
            return ResponseEntity.ok().body(new GetVideoTransactionsResponse(null, videoTransactions));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_PURCHASE_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetVideoTransactionsResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getTreesEarned(String videoOwnerUserId) {
        if (videoOwnerUserId == null || videoOwnerUserId.isEmpty()) {
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
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new IsVideoPurchasedByUserResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."), false));
        }
        if (videoId == null || videoId.isEmpty()) {
            return ResponseEntity.badRequest().body(new IsVideoPurchasedByUserResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "video parameter is required."), false));
        }
        try {
            Boolean isPurchased = isVideoPurchasedByUser(userId, videoId);
            return ResponseEntity.ok().body(new IsVideoPurchasedByUserResponse(null, isPurchased));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.IS_VIDEO_PURCHASED, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new IsVideoPurchasedByUserResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), false));
        }
    }


    public ResponseEntity<?> videoEarningAndSales(String videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return ResponseEntity.badRequest().body(new VideoEarningsAndSalesResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "video parameter is required."), null));
        }
        try {
            List<String> videoIdsList = Arrays.stream(videoIds.split(","))
                    .toList();
            Map<String, VideoEarningsAndSales> videoStats = getVideoStats(videoIdsList);
            return ResponseEntity.ok().body(new VideoEarningsAndSalesResponse(null, videoStats));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_EARNING_AND_SALES, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VideoEarningsAndSalesResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }
}
