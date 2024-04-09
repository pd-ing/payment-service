package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientLeafsException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.CallDetails;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.repository.CallRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CallChargeService {
    @Autowired
    CallRepository callRepository;

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


    @Transactional
    String CreateCallTransaction(String userId, String pddUserId, BigDecimal leafsToCharge, TransactionType callType) {
        walletService.deductLeafsFromWallet(userId, leafsToCharge);

        CallDetails callDetails = new CallDetails(userId, pddUserId, leafsToCharge, callType);
        callRepository.save(callDetails);

        earningService.addLeafsToEarning(pddUserId, leafsToCharge);
        ledgerService.saveToLedger(callDetails.getId(), new BigDecimal(0), leafsToCharge, callType);

        return "Leafs charge was successful";
    }

    List<CallDetails> fetchCallDetailsHistoryForPd(String pdUserId) {
        return callRepository.findByPdUserId(pdUserId);
    }

    List<CallDetails> fetchCallDetailsHistoryForUser(String userId) {
        return callRepository.findByUserId(userId);
    }

    List<PublicUserNet> getTopCallersForPdInfo(String pdUserId, Long limit) throws Exception {
        List<Object[]> callerUserObjects = callRepository.findTopCallerUserByPdUserID(pdUserId, limit);
        return getPublicUserInfo(callerUserObjects);
    }

    List<PublicUserNet> getTopCallersInfo(String userId, Long limit) throws Exception {
        List<Object[]> callerUserObjects = callRepository.findTopCallerUsers(userId, limit);
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

    public ResponseEntity<?> buyCall(String pdUserId, BigDecimal leafsToCharge, String callType) {

        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."), null));
        }

        if (leafsToCharge == null) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafsToCharge parameter is required."), null));
        }

        if ((leafsToCharge.compareTo(BigDecimal.ZERO) == 0) || (leafsToCharge.compareTo(BigDecimal.ZERO) < 0)) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafsToCharge should be greater than 0"), null));
        }

        TransactionType transactionType;
        if (callType.equals("audio")) {
            transactionType = TransactionType.AUDIO_CALL;
        } else if (callType.equals("video")) {
            transactionType = TransactionType.VIDEO_CALL;
        } else if (callType.equals("message")) {
            transactionType = TransactionType.TEXT_MESSAGE;
        } else {
            return ResponseEntity.badRequest().body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid callType passed,Valid callTyle is audio or video"), null));
        }

        try {
            String userId = authHelper.getUserId();
            String message = CreateCallTransaction(userId, pdUserId, leafsToCharge, transactionType);
            return ResponseEntity.ok().body(new GenericStringResponse(null, message));
        } catch (WalletNotFoundException e) {
            pdLogger.logException(PdLogger.EVENT.CALL_CHARGE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientLeafsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericStringResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.CALL_CHARGE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    public ResponseEntity<?> callDetailsHistoryForPd(String pdUserId) {
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."), null));
        }

        try {
            List<CallDetails> callDetails = fetchCallDetailsHistoryForPd(pdUserId);
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
            List<CallDetails> callDetails = fetchCallDetailsHistoryForUser(userId);
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
