package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.net.VideoPurchaserInfo;
import com.pding.paymentservice.payload.response.BuyVideoResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GetVideoTransactionsResponse;
import com.pding.paymentservice.payload.response.IsVideoPurchasedByUserResponse;
import com.pding.paymentservice.payload.response.custompagination.PaginationInfoWithGenericList;
import com.pding.paymentservice.payload.response.custompagination.PaginationResponse;
import com.pding.paymentservice.payload.response.TotalTreesEarnedResponse;
import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;
import com.pding.paymentservice.payload.response.VideoEarningsAndSalesResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.videoSales.DailyTreeRevenueResponse;
import com.pding.paymentservice.payload.response.videoSales.VideoSalesHistoryRecord;
import com.pding.paymentservice.payload.response.videoSales.VideoSalesHistoryResponse;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.EmailValidator;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    private EmailValidator emailValidator;

    @Autowired
    private TokenSigner tokenSigner;


    @Transactional
    public VideoPurchase createVideoTransaction(String userId, String videoId, BigDecimal treesToConsumed, String videoOwnerUserId) {
        walletService.deductTreesFromWallet(userId, treesToConsumed);

        VideoPurchase transaction = new VideoPurchase(userId, videoId, treesToConsumed, videoOwnerUserId);
        VideoPurchase video = videoPurchaseRepository.save(transaction);
        pdLogger.logInfo("BUY_VIDEO", "Video purchase record created with details UserId : " + userId + " ,VideoId : " + videoId + ", trees : " + treesToConsumed + ", VideoOwnerUserId : " + videoOwnerUserId);

        earningService.addTreesToEarning(videoOwnerUserId, treesToConsumed);
        ledgerService.saveToLedger(video.getId(), treesToConsumed, new BigDecimal(0), TransactionType.VIDEO_PURCHASE, userId);
        pdLogger.logInfo("BUY_VIDEO", "Video purchase details recorded in LEGDER VideoId : " + videoId + ", trees : " + treesToConsumed + ", TransactionType : " + TransactionType.VIDEO_PURCHASE);

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
        pdLogger.logInfo("BUY_VIDEO_REPLACEMENT", "Video purchase record created with details UserId : " + userId + " ,VideoId : " + videoId + ", trees : " + treesToConsumed + ", VideoOwnerUserId : " + videoOwnerUserId);
    }

    public List<VideoPurchase> getAllTransactionsForUser(String userID) {
        return videoPurchaseRepository.getVideoPurchaseByUserId(userID);
    }

    public BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserID) {
        return videoPurchaseRepository.getTotalTreesEarnedByVideoOwner(videoOwnerUserID);
    }

    public BigDecimal getDailyTreeRevenueByVideoOwner(String videoOwnerUserID, LocalDateTime endDateTime) {
        return videoPurchaseRepository.getDailyTreeRevenueByVideoOwner(videoOwnerUserID,endDateTime);
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

    public ResponseEntity<?> getVideoTransactions(String pdId, int page, int size, int sort) {
        try {
            String userId = authHelper.getUserId();
            Page<VideoPurchase> videoTransactions = Page.empty();
            if (sort == 0 || sort == 1) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(sort == 0 ? Sort.Direction.ASC : Sort.Direction.DESC, "lastUpdateDate"));
                if (pdId == null) {
                    videoTransactions = videoPurchaseRepository.findByUserId(userId, pageable);
                } else {
                    videoTransactions = videoPurchaseRepository.findByUserIdAndVideoOwnerUserId(userId, pdId, pageable);
                }
            } else if (sort == 2) {

            }

            return ResponseEntity.ok().body(new GetVideoTransactionsResponse(null, videoTransactions.toList()));
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
                res.add(new VideoPurchaserInfo(p.getEmail(), v.getUserId(), null, date));
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

    public ResponseEntity<?> getSalesHistoryOfUser(LocalDate startDate, LocalDate endDate, int page, int size, int sort) {
        try {
            String userId = authHelper.getUserId();
            List<VideoSalesHistoryRecord> shList = null;
            if (sort == 0 || sort == 1) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(sort == 0 ? Sort.Direction.ASC : Sort.Direction.DESC, "last_update_date"));
                if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                    return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
                }
                if (userId == null) {
                    return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "UserId null; cannot get video sales history."));
                } else {
                    Page<Object[]> shPage = videoPurchaseRepository.getSalesHistoryByUserIdAndDates(userId, startDate, endDate, pageable);
                    shList = createSalesHistoryList(shPage.getContent());
                }
            }
            return ResponseEntity.ok().body(new VideoSalesHistoryResponse(null, shList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_PURCHASE_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VideoSalesHistoryResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> searchSalesHistoryOfUser( String searchString, int page, int size, int sort) {
        try {
            String userId = authHelper.getUserId();
            List<VideoSalesHistoryRecord> shList = null;
            if (sort == 0 || sort == 1) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(sort == 0 ? Sort.Direction.ASC : Sort.Direction.DESC, "last_update_date"));
                Page<Object[]> shPage = videoPurchaseRepository.searchSalesHistoryByUserId(userId, searchString, pageable);
                shList = createSalesHistoryList(shPage.getContent());
            }
            return ResponseEntity.ok().body(new VideoSalesHistoryResponse(null, shList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.VIDEO_PURCHASE_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VideoSalesHistoryResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
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

            List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIdsPage.toSet()).blockFirst();
            if (usersFlux == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error getting user details from user service."));
            }

            for (PublicUserNet user : usersFlux) {
                String profilePicture = null;
                String coverImage = null;

                try {
                    if (user.getProfilePicture() != null) {
                        profilePicture = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getProfilePicture()), 8);
                    }
                    if (user.getCoverImage() != null) {
                        coverImage = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getCoverImage()), 8);
                    }
                } catch (Exception e) {}

                user.setProfilePicture(profilePicture);
                user.setCoverImage(coverImage);
            }

            Page<PublicUserNet> resData = new PageImpl<>(usersFlux, pageable, userIdsPage.getTotalElements());

            return ResponseEntity.ok().body(new GenericPageResponse<>(null, resData));

        } catch (Exception ex) {
            pdLogger.logException(ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
        }
    }


}
