package com.pding.paymentservice.service;

import com.google.common.io.Files;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.VideoPurchaseDuration;
import com.pding.paymentservice.models.other.services.tables.dto.DonorData;
import com.pding.paymentservice.models.other.services.tables.dto.VideoDurationPriceDTO;
import com.pding.paymentservice.models.report.ReportGenerationCompletedEvent;
import com.pding.paymentservice.models.report.ReportGenerationFailedEvent;
import com.pding.paymentservice.models.report.ReportGenerationInProgressEvent;
import com.pding.paymentservice.models.report.ReportGenerationStartedEvent;
import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.dto.VideoPurchaseLiteDTO;
import com.pding.paymentservice.payload.dto.VideoSaleHistory;
import com.pding.paymentservice.payload.dto.VideoSaleHistorySummary;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.net.VideoPurchaserInfo;
import com.pding.paymentservice.payload.response.BuyVideoResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GetVideoTransactionsResponse;
import com.pding.paymentservice.payload.response.IsVideoPurchasedByUserResponse;
import com.pding.paymentservice.payload.response.IsVideoPurchasedByUserResponseV2;
import com.pding.paymentservice.payload.response.PaidUnpaidFollowerCountResponse;
import com.pding.paymentservice.payload.response.PaidUnpaidFollowerResponse;
import com.pding.paymentservice.payload.response.TotalTreesEarnedResponse;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.payload.response.VideoEarningsAndSalesResponse;
import com.pding.paymentservice.payload.response.VideoPurchaseTimeRemainingResponse;
import com.pding.paymentservice.payload.response.SalesHistoryData;
import com.pding.paymentservice.payload.response.custompagination.PaginationInfoWithGenericList;
import com.pding.paymentservice.payload.response.custompagination.PaginationResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.payload.response.videoSales.DailyTreeRevenueResponse;
import com.pding.paymentservice.payload.response.videoSales.VideoSalesHistoryRecord;
import com.pding.paymentservice.payload.response.videoSales.VideoSalesHistoryResponse;
import com.pding.paymentservice.repository.GenerateReportEvent;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    @Autowired
    private UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    private EmailValidator emailValidator;

    @Autowired
    private TokenSigner tokenSigner;

    @Autowired
    SendNotificationService sendNotificationService;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    @Autowired
    AsyncOperationService asyncOperationService;

    @Autowired
    PDFService pdfService;

    @Autowired
    EmailSenderService emailSenderService;

    @Transactional
    public VideoPurchase createVideoTransaction(String userId, String videoId, BigDecimal treesToConsumed, String videoOwnerUserId) {
        log.info("Buy video request made with following details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}", userId, videoId, treesToConsumed, videoOwnerUserId);
        walletService.deductTreesFromWallet(userId, treesToConsumed);

        VideoPurchase transaction = new VideoPurchase(userId, videoId, treesToConsumed, videoOwnerUserId);
        VideoPurchase video = videoPurchaseRepository.save(transaction);
        log.info("Video purchase record created with details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}", userId, videoId, treesToConsumed, videoOwnerUserId);

        earningService.addTreesToEarning(videoOwnerUserId, treesToConsumed);
        ledgerService.saveToLedger(video.getId(), treesToConsumed, new BigDecimal(0), TransactionType.VIDEO_PURCHASE, userId);
        log.info("Buy video request transaction completed with details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}", userId, videoId, treesToConsumed, videoOwnerUserId);
        return video;
    }

    @Transactional
    public VideoPurchase createVideoTransaction(String userId, String videoId, String videoOwnerUserId, BigDecimal treesToConsumed, String duration) {
        log.info("Buy video request made with following details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}, duration : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(videoId), LogSanitizer.sanitizeForLog(treesToConsumed), LogSanitizer.sanitizeForLog(videoOwnerUserId), LogSanitizer.sanitizeForLog(duration));
        walletService.deductTreesFromWallet(userId, treesToConsumed);

        VideoPurchase transaction = new VideoPurchase(userId, videoId, treesToConsumed, videoOwnerUserId, duration,
                VideoPurchaseDuration.valueOf(duration).getExpiryDate());

        VideoPurchase video = videoPurchaseRepository.save(transaction);

        earningService.addTreesToEarning(videoOwnerUserId, treesToConsumed);
        ledgerService.saveToLedger(video.getId(), treesToConsumed, new BigDecimal(0), TransactionType.VIDEO_PURCHASE, userId);
        log.info("Buy video request transaction completed with details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}, duration : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(videoId), LogSanitizer.sanitizeForLog(treesToConsumed), LogSanitizer.sanitizeForLog(videoOwnerUserId), LogSanitizer.sanitizeForLog(duration));
        return video;
    }

    public ResponseEntity<?> createVideoPurchaseReplacementFromEmail(String videoId, String ownerUserId, String emails) {
        try {
            String ownerId;
            if (ownerUserId == null) {
                ownerId = authHelper.getUserId();
            } else {
                ownerId = ownerUserId;
            }
            List<String> validEmails = Arrays.stream(emails.split(","))
                    .filter(e -> emailValidator.isValidEmail(e))
                    .toList();
            List<String> userIds = userServiceNetworkManager.getUsersListByEmailFlux(validEmails)
                    .map(PublicUserNet::getId) // Transform PublicUserNet to its id
                    .collect(Collectors.toList()) // Collect ids into a List
                    .block();

            if (userIds == null || userIds.isEmpty()) {
                return ResponseEntity.ok("No users added. Check the emails you added are valid. Or Try again. Or contact support.");
            }
            List<String> ids = createVideoReplacements(videoId, ownerId, userIds);
            if (ids.isEmpty()) {
                return ResponseEntity.ok("Added all users");
            } else {
                return ResponseEntity.ok("Added all users except: " + String.join(",", ids));
            }

        } catch (Exception ex) {
            pdLogger.logException(ex);
            int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            return ResponseEntity.status(code).body(new ErrorResponse(code, ex.getMessage()));
        }
    }

    /**
     * @param videoId      - video id
     * @param videoOwnerId - owner of the video
     * @param userIds      - list of user ids
     * @return the failed user ids to add in the video purchase table as replacement.
     */
    public List<String> createVideoReplacements(String videoId, String videoOwnerId, List<String> userIds) {
        List<String> failedUIds = new ArrayList<>(List.of());
        userIds.forEach(uid -> {
            try {
                createVideoTransactionForVideoReplacement(uid, videoId, new BigDecimal(0), videoOwnerId);
            } catch (Exception ex) {
                failedUIds.add(uid);
                pdLogger.logException(ex);
            }
        });
        return failedUIds;

    }

    @Transactional
    public void createVideoTransactionForVideoReplacement(String userId, String videoId, BigDecimal treesToConsumed, String videoOwnerUserId) {
        walletService.deductTreesFromWallet(userId, treesToConsumed);

        VideoPurchase transaction = new VideoPurchase(userId, videoId, treesToConsumed, videoOwnerUserId, true);
        VideoPurchase video = videoPurchaseRepository.save(transaction);
//        pdLogger.logInfo("BUY_VIDEO_REPLACEMENT", "Video purchase record created with details UserId : " + userId + " ,VideoId : " + videoId + ", trees : " + treesToConsumed + ", VideoOwnerUserId : " + videoOwnerUserId);
    }

    public List<VideoPurchase> getAllTransactionsForUser(String userID) {
        return videoPurchaseRepository.getVideoPurchaseByUserId(userID);
    }

    public BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserID) {
        return videoPurchaseRepository.getTotalTreesEarnedByVideoOwner(videoOwnerUserID);
    }

    public BigDecimal getDailyTreeRevenueByVideoOwner(String videoOwnerUserID, LocalDateTime endDateTime) {
        return videoPurchaseRepository.getDailyTreeRevenueByVideoOwner(videoOwnerUserID, endDateTime);
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

    public ResponseEntity<?> buyVideoV3(String videoId, String duration) {
        if (videoId == null || videoId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "videoid parameter is required."));
        }
        if (duration == null || duration.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "duration parameter is required."));
        }

        try {
            String userId = authHelper.getUserId();
            String videoOwnerUserId = otherServicesTablesNativeQueryRepository.findUserIdByVideoId(videoId).orElse(null);

            if (videoOwnerUserId == null || videoOwnerUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), " VideoOwnerUserID is null or empty for the videoId provided"), null));
            }

            //get trees
            List<VideoDurationPriceDTO> prices = new ArrayList<>();
            List<Object[]> pricesRawResults = otherServicesTablesNativeQueryRepository.findPricesByVideoId(videoId);
            for (Object[] row : pricesRawResults) {
                VideoDurationPriceDTO price = new VideoDurationPriceDTO();
                price.setVideoId((String) row[0]);
                price.setDuration((String) row[1]);
                price.setTrees((BigDecimal) row[2]);
                price.setEnabled((Boolean) row[3]);
                prices.add(price);
            }

            Map<String, VideoDurationPriceDTO> mapDurationPrice = prices.stream().collect(Collectors.toMap(VideoDurationPriceDTO::getDuration, v -> v));
            VideoDurationPriceDTO price = mapDurationPrice.get(duration);

            if (price == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Can not find trees for this duration"), null));
            } else if (!price.getEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "This duration is not enabled"), null));
            }


            List<VideoPurchase> videoPurchases = videoPurchaseRepository.findByUserIdAndVideoId(userId, videoId);

            //check if video with duration not expired and already purchased
            if (videoPurchases.stream().anyMatch(vp -> vp.getExpiryDate() == null || vp.getExpiryDate().isAfter(LocalDateTime.now()))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Video already purchased"), null));
            }

            VideoPurchase video = createVideoTransaction(userId, videoId, videoOwnerUserId, price.getTrees(), price.getDuration());

            sendNotificationService.sendBuyVideoNotification(video);

            asyncOperationService.removeCachePattern("purchasedVideos::" + videoOwnerUserId + "," + userId + "*");
            asyncOperationService.removeCachePattern("videos::" + videoOwnerUserId + "," + userId + "*");
            asyncOperationService.removeCachePattern("videos::" + videoOwnerUserId + "," + videoOwnerUserId + "*");

            return ResponseEntity.ok().body(new BuyVideoResponse(null, video));
        } catch (WalletNotFoundException e) {
            pdLogger.logException(PdLogger.EVENT.BUY_VIDEO, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BuyVideoResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
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
            return ResponseEntity.ok().body(new GetVideoTransactionsResponse(null, videoTransactions, null));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_PURCHASE_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetVideoTransactionsResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null, null));
        }
    }

    //    @Cacheable(value = "notExpiredVideo", key = "{#userId, #pdId, #page, #size}", cacheManager = "cacheManager")
    public GetVideoTransactionsResponse getVideoTransactions(String userId, String pdId, int page, int size, int sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort == 0 ? Sort.Direction.ASC : Sort.Direction.DESC, "lastUpdateDate"));
        Page<VideoPurchase> videoTransactions = videoPurchaseRepository.findNotExpiredVideo(userId, pdId, pageable);
        return new GetVideoTransactionsResponse(null, videoTransactions.toList(), videoTransactions.hasNext());

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

    public ResponseEntity<?> isVideoPurchasedV2(String userId, String videoId) {
        try {
            List<VideoPurchase> videoPurchases = videoPurchaseRepository.findByUserIdAndVideoId(userId, videoId);

            if (videoPurchases == null || videoPurchases.isEmpty()) {
                return ResponseEntity.ok().body(new IsVideoPurchasedByUserResponseV2(videoId, false, null, null));
            }

            //video purchase before timing duration release is permanent as default
            if (videoPurchases.stream().anyMatch(vp -> vp.getDuration() == null || vp.getExpiryDate() == null)) {
                return ResponseEntity.ok().body(new IsVideoPurchasedByUserResponseV2(videoId, true, LocalDateTime.now().plusYears(100), true));
            }

            Optional<VideoPurchase> videoPurchase = videoPurchases.stream()
                    .filter(vp -> vp.getExpiryDate() != null && vp.getExpiryDate().isAfter(LocalDateTime.now()))
                    .findFirst();

            if (videoPurchase.isPresent()) {
                Boolean isPermanent = VideoPurchaseDuration.PERMANENT.name().equalsIgnoreCase(videoPurchase.get().getDuration());
                return ResponseEntity.ok().body(new IsVideoPurchasedByUserResponseV2(videoId, true, videoPurchase.get().getExpiryDate(), isPermanent));
            } else {
                //get latest expiry date
                LocalDateTime latestExpiryDate = videoPurchases.stream()
                        .map(VideoPurchase::getExpiryDate)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                return ResponseEntity.ok().body(new IsVideoPurchasedByUserResponseV2(videoId, false, latestExpiryDate, false));
            }
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.IS_VIDEO_PURCHASED, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new IsVideoPurchasedByUserResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), false));
        }
    }

//    public ResponseEntity<?> getPaidUnpaidFollowerList(String userId) {
//        if (userId == null || userId.isEmpty()) {
//            return ResponseEntity.badRequest().body(new PaidUnpaidFollowerResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."), null, null));
//        }
//        try {
//            String paidUsers = "";
//            String unpaidUsers = "";
//
//            List<Object[]> followerList = videoPurchaseRepository.getFollowersList(userId);
//            for (Object[] followerRecord : followerList) {
//                if(followerRecord[1] == null)
//                    unpaidUsers = unpaidUsers.trim() + followerRecord[0].toString() + ", ";
//                else
//                    paidUsers = paidUsers.trim() + followerRecord[0].toString() + ", ";
//            }
//
//            // Remove the trailing comma and space if they exist
//            if (paidUsers.length() > 0) {
//                paidUsers = paidUsers.substring(0, paidUsers.length() - 2);
//            }
//            if (unpaidUsers.length() > 0) {
//                unpaidUsers = unpaidUsers.substring(0, unpaidUsers.length() - 2);
//            }
//
//            return ResponseEntity.ok().body(new PaidUnpaidFollowerResponse(null, paidUsers, unpaidUsers));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PaidUnpaidFollowerResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null, null));
//        }
//    }

    public ResponseEntity<?> getPaidUnpaidFollowerCount(String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new PaidUnpaidFollowerResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."), null, null));
        }
        try {
            List<String> paidUsers = new ArrayList<String>();
            List<String> unpaidUsers = new ArrayList<String>();

            List<Object[]> followerList = videoPurchaseRepository.getFollowersList(userId);
            for (Object[] followerRecord : followerList) {
                if (followerRecord[1] == null)
                    unpaidUsers.add(followerRecord[0].toString());
                else
                    paidUsers.add(followerRecord[0].toString());
            }

            return ResponseEntity.ok().body(new PaidUnpaidFollowerCountResponse(null, BigInteger.valueOf(paidUsers.size()), BigInteger.valueOf(unpaidUsers.size())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PaidUnpaidFollowerCountResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), BigInteger.valueOf(0), BigInteger.valueOf(0)));
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

    public ResponseEntity<?> loadPurchaseListOfSellerResponse(String videoId, int page, int size) {
        try {
            return ResponseEntity.ok(new PaginationResponse(null, loadPurchaseListOfSeller(videoId, page, size)));
        } catch (Exception ex) {
            pdLogger.logException(ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
        }
    }

    public ResponseEntity<?> loadPurchaseListOfSellerResponse(String videoId, String onlyForTheseUsersList, int page, int size) {
        try {
            List<String> users = Arrays.stream(onlyForTheseUsersList.split(",")).toList().stream().map(String::trim).toList();
            return ResponseEntity.ok(loadPurchaseListOfSellerOnlyForSomeUsers(videoId, users, page, size));
        } catch (Exception ex) {
            pdLogger.logException(ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
        }
    }

    private PaginationInfoWithGenericList<VideoPurchaserInfo> convertToResponse(Page<VideoPurchase> dataList) throws Exception {
        List<VideoPurchase> dataContent = dataList.getContent();
        Set<String> userIds = dataContent.stream().parallel().map(VideoPurchase::getUserId).collect(Collectors.toSet());

        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIds).blockFirst();

        if (usersFlux == null) {
            return new PaginationInfoWithGenericList<>(
                    dataList.getNumber(),
                    dataList.getSize(),
                    dataList.getTotalElements(),
                    dataList.getTotalPages(),
                    List.of()
            );
        }

        Map<String, PublicUserNet> userMap = usersFlux.stream().parallel().collect(Collectors.toMap(PublicUserNet::getId, user -> user));

        List<VideoPurchaserInfo> res = new ArrayList<>();

        dataContent.forEach((v) -> {
            PublicUserNet p = userMap.get(v.getUserId());
            if (p != null) {
                String date = v.getLastUpdateDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                res.add(new VideoPurchaserInfo(p.getEmail(), v.getUserId(), null, date, v.getDuration(), v.getExpiryDate(), v.getTreesConsumed()));
            }
        });

        return new PaginationInfoWithGenericList<>(
                dataList.getNumber(),
                dataList.getSize(),
                dataList.getTotalElements(),
                dataList.getTotalPages(),
                res
        );
    }

    private PaginationInfoWithGenericList<VideoPurchaserInfo> loadPurchaseListOfSeller(String videoId, int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<VideoPurchase> pageData = videoPurchaseRepository.findAllByVideoIdOrderByLastUpdateDateDesc(videoId, pageRequest);

            return convertToResponse(pageData);
        } catch (Exception ex) {
            pdLogger.logException(ex);
            return null;
        }
    }

    private List<VideoPurchaserInfo> loadPurchaseListOfSellerOnlyForSomeUsers(String videoId, List<String> onlyForTheseUsersList, int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<VideoPurchase> pageData = videoPurchaseRepository.findAllByVideoIdAndUserIdInOrderByLastUpdateDateDesc(videoId, onlyForTheseUsersList, pageRequest);

            return convertToResponse(pageData).getContent();
        } catch (Exception ex) {
            pdLogger.logException(ex);
            return null;
        }
    }

    public ResponseEntity<?> getSalesHistoryOfUser(String searchString, LocalDate startDate, LocalDate endDate, int page, int size, int sort) {
        try {
            String userId = authHelper.getUserId();
            List<VideoSalesHistoryRecord> shList = null;
            Long totalTreesEarned = 0l;
            Pageable pageable = null;
            Page<Object[]> shPage = null;
            if (sort == 0 || sort == 1) {
                pageable = PageRequest.of(page, size, Sort.by(sort == 0 ? Sort.Direction.ASC : Sort.Direction.DESC, "last_update_date"));
                if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                    return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
                }
                if (userId == null) {
                    return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "UserId null; cannot get video sales history."));
                } else {
                    shPage = videoPurchaseRepository.getSalesHistoryByUserIdAndDates(searchString, userId, startDate, endDate, pageable);
                    shList = createSalesHistoryList(shPage.getContent());
                    totalTreesEarned = videoPurchaseRepository.getTotalTreesEarned(userId, startDate, endDate);
                }
            }
            return ResponseEntity.ok().body(new VideoSalesHistoryResponse(null, totalTreesEarned, new PageImpl<>(shList, pageable, shPage.getTotalElements())));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_PURCHASE_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VideoSalesHistoryResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null, null));
        }
    }

    private List<VideoSalesHistoryRecord> createSalesHistoryList(List<Object[]> shPage) {
        List<VideoSalesHistoryRecord> shList = new ArrayList<>();
        for (Object innerObject : shPage) {
            Object[] salesHistory = (Object[]) innerObject;
            VideoSalesHistoryRecord shObj = new VideoSalesHistoryRecord();
            shObj.setBuyerEmail(salesHistory[3].toString());
            shObj.setVideoTitle(salesHistory[1].toString());
            shObj.setAmount(salesHistory[2].toString());
            shObj.setPurchaseDate(salesHistory[0].toString());
            shObj.setDuration(salesHistory[4].toString());
            shObj.setExpiryDate(salesHistory[5].toString());
            shList.add(shObj);
        }
        return shList;
    }

    public ResponseEntity<?> getDailyTreeRevenueOfUser(String userId, LocalDateTime endTime) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new DailyTreeRevenueResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."), new BigDecimal(0.0)));
        }
        if (endTime == null) {
            return ResponseEntity.badRequest().body(new DailyTreeRevenueResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "end time is required."), new BigDecimal(0.0)));
        }
        try {

            BigDecimal dailyTreeRevenue = getDailyTreeRevenueByVideoOwner(userId, endTime);
            return ResponseEntity.ok().body(new DailyTreeRevenueResponse(null, dailyTreeRevenue));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_PURCHASE_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DailyTreeRevenueResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getAllPdUserIdWhoseVideosArePurchasedByUser(int size, int page) {
        try {
            String userId = authHelper.getUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Authenticated request. Invalid user"));
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<String> userIdsPage = videoPurchaseRepository.getAllPdUserIdWhoseVideosArePurchasedByUser(userId, pageable);

            if (userIdsPage.isEmpty()) {
                Page<UserLite> resData = new PageImpl<>(List.of(), pageable, userIdsPage.getTotalElements());
                return ResponseEntity.ok().body(new GenericPageResponse<>(null, resData));
            }

            List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIdsPage.toSet()).blockFirst();
            if (usersFlux == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error getting user details from user service."));
            }

            List<UserLite> users = usersFlux.stream().map(userObj -> {
                return UserLite.fromPublicUserNet(userObj, tokenSigner);
            }).toList();

            Page<UserLite> resData = new PageImpl<>(users, pageable, userIdsPage.getTotalElements());

            return ResponseEntity.ok().body(new GenericPageResponse<>(null, resData));

        } catch (Exception ex) {
            pdLogger.logException(ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
        }
    }

    public ResponseEntity<?> getVideoPurchaseTimeRemaining(String userId, Set<String> videoIds) {
        try {
            List<VideoPurchase> videoPurchases;
            if (videoIds == null || videoIds.isEmpty()) {
                videoPurchases = videoPurchaseRepository.findByUserId(userId);
            } else {
                videoPurchases = videoPurchaseRepository.findByUserIdAndVideoIdIn(userId, videoIds);
            }
            Map<String, List<VideoPurchase>> videoPurchasesMap = videoPurchases.stream().collect(Collectors.groupingBy(VideoPurchase::getVideoId, Collectors.toList()));
            List<VideoPurchaseTimeRemainingResponse> videoPurchaseTimeRemainingList =
                    videoPurchasesMap.entrySet().stream().map(entry -> {
                        VideoPurchase vp;
                        Optional<VideoPurchase> vpOptional = entry.getValue().stream().filter(v -> v.getExpiryDate() == null).findAny();
                        if (vpOptional.isPresent()) vp = vpOptional.get();
                        else {
                            vp = entry.getValue().stream().max(Comparator.comparing(VideoPurchase::getExpiryDate)).get();
                        }

                        VideoPurchaseTimeRemainingResponse vpTimeRemaining = new VideoPurchaseTimeRemainingResponse();
                        vpTimeRemaining.setVideoId(entry.getKey());
                        vpTimeRemaining.setExpiryDate(vp.getExpiryDate());
                        vpTimeRemaining.setIsPermanent(vp.getExpiryDate() == null || vp.getDuration() == null || vp.getDuration().equals(VideoPurchaseDuration.PERMANENT.name()));
                        vpTimeRemaining.setIsExpirated(vp.getExpiryDate() != null && vp.getExpiryDate().isBefore(LocalDateTime.now()));
                        vpTimeRemaining.setNumberOfDaysRemaining(vp.getExpiryDate() != null ? ChronoUnit.DAYS.between(LocalDateTime.now(), vp.getExpiryDate()) : null);
                        return vpTimeRemaining;
                    }).collect(Collectors.toList());


            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, videoPurchaseTimeRemainingList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_PURCHASE_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    //    @Cacheable(value = "expiredVideoPurchases", key = "#userId + #creatorUserId + #page + #pageSize", cacheManager = "cacheManager")
    public GetVideoTransactionsResponse expiredVideoPurchases(String userId, String creatorUserId, int page, int pageSize, int sort) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sort == 0 ? Sort.Direction.ASC : Sort.Direction.DESC, "maxExpiryDate"));
        Page<VideoPurchase> videoPurchases = videoPurchaseRepository.findExpiredVideoPurchases(userId, creatorUserId, pageable);
        return new GetVideoTransactionsResponse(null, videoPurchases.toList(), videoPurchases.hasNext());
    }

    public VideoSaleHistorySummary getVideoSaleSummary(String videoId) {
        Long totalSales = videoPurchaseRepository.countByVideoId(videoId);
        Long totalUserBuyVideo = videoPurchaseRepository.countUserBuyVideo(videoId);
        Long totalRePurchased = totalSales - totalUserBuyVideo;
        return new VideoSaleHistorySummary(totalSales, totalRePurchased);
    }

    public Page<VideoSaleHistory> getVideoPurchaseHistory(String videoId, Pageable pageable) {
        return videoPurchaseRepository.getSaleHistoryByVideoId(videoId, pageable)
                .map(objects -> {
                    String vId = (String) objects[0];
                    String videoOwnerUserId = (String) objects[1];
                    String userEmail = (String) objects[2];
                    String userId = (String) objects[3];
                    Integer totalSales = (Integer) objects[4];
                    List<String> purchaseDateStrList = Arrays.asList(((String) objects[5]).split(","));
                    List<String> durationList = Arrays.asList(((String) objects[6]).split(","));
                    List<String> expiryDateList = Arrays.asList(((String) objects[7]).split(","));
                    List<BigDecimal> treesConsumedList = Arrays.asList(((String) objects[8]).split(",")).stream().map(BigDecimal::new).collect(Collectors.toList());

                    List<LocalDateTime> purchaseDateList = purchaseDateStrList.stream().map(s -> LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))).collect(Collectors.toList());
                    List<LocalDateTime> expiryDateListLocalDateTime = expiryDateList.stream().map(s -> LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))).collect(Collectors.toList());

                    List<VideoPurchaseLiteDTO> purchaseLiteDTOList = new ArrayList<>();
                    for (int i = 0; i < totalSales; i++) {
                        purchaseLiteDTOList.add(new VideoPurchaseLiteDTO(durationList.get(i), treesConsumedList.get(i), purchaseDateList.get(i), expiryDateListLocalDateTime.get(i)));
                    }

                    return new VideoSaleHistory(vId, videoOwnerUserId, userEmail, userId, totalSales, purchaseLiteDTOList);
                });
    }


    private SalesHistoryData getAllSalesHistoryByDate(
            String userId,
            String email,
            String searchString,
            LocalDate startDate,
            LocalDate endDate,
            int sort
    ) {
        if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
            throw new IllegalArgumentException("Both start date and end date should either be null or have a value");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Both start date and end date cannot be null at the same time");
        }
        endDate = endDate.plusDays(1L);
        if (StringUtils.isBlank(searchString)) {
            searchString = null;
        }
        // Create pageable object
        String sortSaleHistory = sort == 0 ? "ASC" : "DESC";
        // Fetch paginated sales history
        List<Object[]> shPage;
        if (userId == null) {
            if (email != null) {
                String id = otherServicesTablesNativeQueryRepository.findUserIdByEmail(email);
                shPage = videoPurchaseRepository.getAllSalesHistoryByUserIdAndDates(searchString, id, startDate, endDate, sortSaleHistory);

            } else {
                throw new IllegalArgumentException("UserId null; cannot get video sales history.");
            }

        } else {
            shPage = videoPurchaseRepository.getAllSalesHistoryByUserIdAndDates(searchString, userId, startDate, endDate, sortSaleHistory);
        }

        // Transform database records into a list of DTOs
        List<VideoSalesHistoryRecord> shList = getSaleHistoryData(shPage);

        // Calculate total trees earned
        Long totalTreesEarned = Optional.ofNullable(videoPurchaseRepository.getTotalTreesEarned(userId, startDate, endDate)).orElse(0L);
        List<Object[]> userInfo = otherServicesTablesNativeQueryRepository.findEmailAndNicknameByUserId(userId);
        String nickname = "";
        if (userInfo != null) {
            for (Object user : userInfo) {
                Object[] dataUser = (Object[]) user;
                email = dataUser!=null? dataUser[0].toString():"";
                nickname = dataUser!=null? dataUser[1].toString():"";
            }
        }
        return new SalesHistoryData(null, totalTreesEarned, userId, email, nickname, shList);
    }

    private List<VideoSalesHistoryRecord> getSaleHistoryData(List<Object[]> shPage) {
        List<VideoSalesHistoryRecord> shList = new ArrayList<>();
        for (Object innerObject : shPage) {
            Object[] salesHistory = (Object[]) innerObject;
            VideoSalesHistoryRecord shObj = new VideoSalesHistoryRecord();
            String email = salesHistory[2].toString();

            shObj.setPurchaseDate(DateTimeUtil.formatLocalDateTime(DateTimeUtil.convertStringToLocaltime(salesHistory[0].toString())));
            shObj.setAmount(salesHistory[1].toString());
            shObj.setBuyerEmail(StringUtil.maskEmail(email));
            shObj.setDuration(StringUtil.convertDurationKeyToValue(salesHistory[3].toString()));
            shObj.setExpiryDate(DateTimeUtil.formatLocalDateTime(DateTimeUtil.convertStringToLocaltime(salesHistory[4].toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            shList.add(shObj);
        }
        return shList;
    }

    public Flux<GenerateReportEvent> salesHistoryDownloadPreparing(String pdUserId, String email, String searchString, LocalDate startDate, LocalDate endDate, int sortOrder) {
        String reportId = UUID.randomUUID().toString();
        Flux<GenerateReportEvent> reportGenerationEvents;
        // Start generate report event
        reportGenerationEvents = salesHistoryListStream(reportId, email, pdUserId, startDate, endDate, searchString, sortOrder);

        return reportGenerationEvents
                .doOnNext(event -> {
                    // handle event
                    if (event instanceof ReportGenerationStartedEvent) {
                        log.info("Report generation started: " + event.getReportId());
                    } else if (event instanceof ReportGenerationInProgressEvent) {
                        log.info("Report is in progress: " + event.getReportId());
                    } else if (event instanceof ReportGenerationCompletedEvent) {
                        log.info("Report generation completed: " + event.getReportId());
                    } else if (event instanceof ReportGenerationFailedEvent) {
                        log.error("Report generation failed: " + event.getReportId());
                    }
                });
    }

    private Flux<GenerateReportEvent> salesHistoryListStream(String reportId, String email, String pdUserId, LocalDate startDate, LocalDate endDate, String searchString, int sortOrder) {
        return Flux.<GenerateReportEvent>create(emitter -> {
            try {
                // Step 1: Report Generation Started
                emitter.next(ReportGenerationStartedEvent.builder()
                        .reportId(reportId)
                        .reportType("Sales History Report")
                        .parameters(Map.of("startDate", startDate, "endDate", endDate))
                        .timestamp(System.currentTimeMillis())
                        .build());

                // Determine the user ID
                String userId = pdUserId != null ? pdUserId : otherServicesTablesNativeQueryRepository.findUserIdByEmail(email);
                if (userId == null) {
                    emitter.next(ReportGenerationFailedEvent.builder()
                            .reportId(reportId)
                            .errorCode("ERROR-001")
                            .errorMessage("User ID or email must be provided.")
                            .failureStep("INITIALIZATION")
                            .timestamp(System.currentTimeMillis())
                            .build());
                    emitter.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID or email must be provided."));
                    return;
                }

                SalesHistoryData salesHistoryData = getAllSalesHistoryByDate(pdUserId, email, searchString, startDate, endDate, sortOrder);
                if (salesHistoryData.getVideoSalesHistoryRecord() == null || salesHistoryData.getVideoSalesHistoryRecord().isEmpty()) {
                    emitter.next(ReportGenerationFailedEvent.builder()
                            .reportId(reportId)
                            .errorCode("ERROR-002")
                            .errorMessage("No sales history data available for the selected period.")
                            .failureStep("INITIALIZATION")
                            .timestamp(System.currentTimeMillis())
                            .build());
                    emitter.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No sales history data available for the selected period."));
                    return;
                }


                ByteArrayOutputStream pdfContent = pdfService.generatePDFSellerHistory(salesHistoryData);
                try {
                    pdfService.cachePdfContent(reportId,pdfContent.toByteArray());
                } catch (IOException e) {
                    emitter.next(ReportGenerationFailedEvent.builder()
                            .reportId(reportId)
                            .errorCode("ERROR-003")
                            .errorMessage(e.getMessage())
                            .failureStep("INITIALIZATION")
                            .errorDetails(Map.of("exception", e.getClass().getName()))
                            .timestamp(System.currentTimeMillis())
                            .build());
                    emitter.error(new RuntimeException(e));
                    return;
                }
                emitter.next(ReportGenerationCompletedEvent.builder()
                        .reportId(reportId)
                        .reportTitle("Sales History Report")
                        .metadata(Map.of("message", "The report has been successfully generated and is ready for download.",
                                "startDate", startDate, "endDate", endDate))
                        .timestamp(System.currentTimeMillis())
                        .build());

                emitter.complete();

            } catch (Exception e) {
                // Emit error if initialization fails
                emitter.next(ReportGenerationFailedEvent.builder()
                        .reportId(reportId)
                        .errorCode("GEN-002")
                        .errorMessage(e.getMessage())
                        .failureStep("INITIALIZATION")
                        .timestamp(System.currentTimeMillis())
                        .errorDetails(Map.of("exception", e.getClass().getName()))
                        .build());
                emitter.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER).subscribeOn(Schedulers.boundedElastic());
    }


    public ResponseEntity<Map<String, String>> salesHistoryDownloadPDF(String reportId, Boolean isSendEmail,HttpServletResponse response) {
        boolean isPdfFetched = false;
        try {
            // Fetch the PDF from storage
            byte[] pdfBytes = pdfService.getPDF(reportId); // Custom service to fetch the PDF

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF not found for reportId: " + reportId);
            }
            isPdfFetched = true;
            String dateTimeNow = DateTimeUtil.getCurrentTimeNow();
            String fileName = "top_sales_history_report_"+dateTimeNow+".pdf";
            if (Boolean.TRUE.equals(isSendEmail)){
                String userId = authHelper.getUserId();
                Optional<String> email = otherServicesTablesNativeQueryRepository.findEmailByUserId(userId);
                if (email.isEmpty()) {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("Error", "Email is required when sending via email.");
                    return ResponseEntity.badRequest().body(responseMap);
                }
                emailSenderService.sendEmailWithAttachmentBytes(email.get(),"Your requested Sales History Report is ready for download.", "PDF Report",fileName,pdfBytes);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "PDF has been sent to the email address.");
                return ResponseEntity.ok(responseMap);
            }else{
                // Return the PDF as a downloadable response
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename="+fileName);
                response.setContentLength(pdfBytes.length);

                response.getOutputStream().write(pdfBytes);
                response.getOutputStream().flush();
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "PDF is ready for download.");
                return ResponseEntity.ok(responseMap);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF not found for reportId: " + reportId, e);
        } finally {
            if(isPdfFetched) pdfService.deletePDF(reportId);
        }
    }

}
