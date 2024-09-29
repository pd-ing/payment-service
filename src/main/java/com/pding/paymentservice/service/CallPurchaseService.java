package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.CallPurchase;
import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.repository.CallPurchaseRepository;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.FirebaseRealtimeDbHelper;
import com.pding.paymentservice.util.TokenSigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CallPurchaseService {
    @Autowired
    CallPurchaseRepository callPurchaseRepository;

    @Autowired
    WalletService walletService;

    @Autowired
    EarningService earningService;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    TokenSigner tokenSigner;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    AuthHelper authHelper;

    @Autowired
    FirebaseRealtimeDbHelper firebaseRealtimeDbHelper;

    @Autowired
    FcmService fcmService;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    @Transactional
    public String CreateCallTransaction(String userId, String pdUserId, BigDecimal leafsToCharge, TransactionType callType, String callId, String giftId, Boolean notifyPd) {
        log.info("Creating call transaction for userId {}, pdUserId {}, leafsToCharge {}, callType {}, callId {}, giftId {}, notifyPd {}",
                userId, pdUserId, leafsToCharge, callType, callId, giftId, notifyPd);
        String returnVal = CreateCallTransactionHelper(userId, pdUserId, leafsToCharge, callType, callId, giftId, notifyPd);

        addCallTransactionEntryToRealTimeDatabase(callId);

        if (notifyPd) {
            try {
                Map<String, String> data = new LinkedHashMap<>();
                data.put("NotificationType", NotificaitonDataType.GIFT_RECEIVE.getDisplayName());
                data.put("GiftId", giftId);
                data.put("pdUserId", pdUserId);
                data.put("userId", userId);
                data.put("leafsTransacted", leafsToCharge.toString());
                data.put("notifyPd", notifyPd.toString());
                data.put("nickname", otherServicesTablesNativeQueryRepository.getNicknameByUserId(userId).orElse("User"));
                fcmService.sendNotification(pdUserId, data);
            } catch (Exception e) {
                pdLogger.logException(e);
            }
        }
        log.info("Call transaction created for userId {}, pdUserId {}, leafsToCharge {}, callType {}, callId {}, giftId {}, notifyPd {}",
                userId, pdUserId, leafsToCharge, callType, callId, giftId, notifyPd);

        return returnVal;
    }

    @Transactional
    private String CreateCallTransactionHelper(String userId, String pdUserId, BigDecimal leafsToCharge, TransactionType callType, String callId, String giftId, Boolean notifyPd) {
        walletService.deductLeafsFromWallet(userId, leafsToCharge);

        CallPurchase callPurchase = new CallPurchase(userId, pdUserId, leafsToCharge, callType, callId, giftId);
        callPurchaseRepository.save(callPurchase);

        earningService.addLeafsToEarning(pdUserId, leafsToCharge);
        ledgerService.saveToLedger(callPurchase.getId(), new BigDecimal(0), leafsToCharge, callType, userId);

        return "Leafs charge was successful";
    }

    public void addCallTransactionEntryToRealTimeDatabase(String callId) {
        log.info("Update call transaction entry to real time database for callId {}", callId);
        try {
            List<Object[]> callPurchaseList = callPurchaseRepository.findUserIdPdUserIdAndSumLeafsTransactedByCallId(callId);
            Map<String, BigDecimal> userLeafsSpentMapping = new HashMap<>();

            BigDecimal leafsToaAdd = new BigDecimal(0);
            for (Object[] callPurchase : callPurchaseList) {
                String userId = callPurchase[0].toString();
                String pdUserID = callPurchase[1].toString();
                String leafsTransacted = callPurchase[2].toString();

                leafsToaAdd = leafsToaAdd.add(new BigDecimal(leafsTransacted)); //Add this balance in PdUserIds Earning Wallet

                // Adding the spending info in Map as in one call there can be multiple users
                BigDecimal balanceBeforeThisTxn = userLeafsSpentMapping.get(userId);
                if (balanceBeforeThisTxn == null) {
                    balanceBeforeThisTxn = new BigDecimal(leafsTransacted);
                } else {
                    balanceBeforeThisTxn = balanceBeforeThisTxn.add(new BigDecimal(leafsTransacted));
                }

                userLeafsSpentMapping.put(userId, balanceBeforeThisTxn);

                firebaseRealtimeDbHelper.updateCallChargesDetailsInFirebase(userId, callId, balanceBeforeThisTxn, new BigDecimal(0));
                firebaseRealtimeDbHelper.updateCallChargesDetailsInFirebase(pdUserID, callId, new BigDecimal(0), leafsToaAdd);
            }

        } catch (Exception e) {
            pdLogger.logException(e);
        }

    }

    List<CallPurchase> fetchCallDetailsHistoryForPd(String pdUserId) {
        return callPurchaseRepository.findByPdUserId(pdUserId);
    }

    List<CallPurchase> fetchCallDetailsHistoryForUser(String userId) {
        return callPurchaseRepository.findByUserId(userId);
    }

    List<PublicUserNet> getTopCallersForPdInfo(String pdUserId, Long limit) throws Exception {
        List<Object[]> callerUserObjects = callPurchaseRepository.findTopCallerUserByPdUserID(pdUserId, limit);
        return getPublicUserInfo(callerUserObjects);
    }

    List<PublicUserNet> getTopCallersInfo(String userId, Long limit) throws Exception {
        List<Object[]> callerUserObjects = callPurchaseRepository.findTopCallerUsers(userId, limit);
        return getPublicUserInfo(callerUserObjects);
    }

    List<PublicUserNet> getPublicUserInfo(List<Object[]> userObjects) throws Exception {
        if (userObjects.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> callerUserIds = userObjects.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());


        Map<String, BigDecimal> topCallersMap = userObjects.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],          // calledUserId
                        row -> (BigDecimal) row[1]       // totalLeafsTransacted
                ));

        List<PublicUserNet> publicUsers = userServiceNetworkManager
                .getUsersListFlux(callerUserIds)
                .collect(Collectors.toList())
                .block();

        for (PublicUserNet user : publicUsers) {
            String profilePicture = null;
            try {
                if (user.getProfilePicture() != null) {
                    profilePicture = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getProfilePicture()), 8);
                }
            } catch (Exception e) {
                pdLogger.logException(PdLogger.EVENT.IMAGE_CDN_LINK, e);
                e.printStackTrace();

            }

            String coverImage = null;
            try {
                if (user.getCoverImage() != null) {
                    coverImage = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getCoverImage()), 8);
                }
            } catch (Exception e) {
                pdLogger.logException(PdLogger.EVENT.IMAGE_CDN_LINK, e);
                e.printStackTrace();

            }

            BigDecimal leafsTransacted = topCallersMap.get(user.getId());

            user.setLeafsTransacted(leafsTransacted);
            user.setProfilePicture(profilePicture);
            user.setCoverImage(coverImage);
        }

        return publicUsers;
    }


    public ResponseEntity<?> callDetailsHistoryForPd(String pdUserId) {
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."), null));
        }

        try {
            List<CallPurchase> callDetails = fetchCallDetailsHistoryForPd(pdUserId);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, callDetails));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.CALL_DETAILS_HISTORY_FOR_PD, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> callDetailsHistoryForUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."), null));
        }

        try {
            List<CallPurchase> callDetails = fetchCallDetailsHistoryForUser(userId);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, callDetails));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.CALL_DETAILS_HISTORY_FOR_USER, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getTopCallersForPd(String pdUserId, Long limit) {
        if (limit == null || limit <= 0 || limit > 30) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "limit parameter is invalid or not passed. Please pass limit between 1-30"));
        }
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }
        try {
            List<PublicUserNet> publicUserNetList = getTopCallersForPdInfo(pdUserId, limit);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.TOP_CALLER_LIST_FOR_PD, e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }

    public ResponseEntity<?> getTopCallers(String userId, Long limit) {
        if (limit == null || limit <= 0 || limit > 30) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "limit parameter is invalid or not passed. Please pass limit between 1-30"));
        }
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userId parameter is required."));
        }
        try {
            List<PublicUserNet> publicUserNetList = getTopCallersInfo(userId, limit);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.TOP_CALLER_LIST, e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }

}
