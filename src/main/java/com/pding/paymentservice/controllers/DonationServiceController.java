package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.InvalidUserException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.models.other.services.tables.dto.DonorData;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.DonationResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.donation.DonationHistoryResponse;
import com.pding.paymentservice.payload.response.donation.DonationHistoryWithVideoStatsResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.DonationService;
import com.pding.paymentservice.service.EarningService;
import com.pding.paymentservice.service.SendNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class DonationServiceController {

    @Autowired
    AuthHelper authHelper;
    @Autowired
    DonationService donationService;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    SendNotificationService sendNotificationService;

    @PostMapping(value = "/donate")
    public ResponseEntity<?> donateTrees(@RequestParam(value = "donorUserId", required = false) String donorUserId, @RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "pdUserId") String pdUserId) {
        return donationService.donateToPd(authHelper.getUserId(), trees, pdUserId);
    }

    @PostMapping(value = "/v2/donate")
    public ResponseEntity<?> donateTreesV2(@RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "pdUserId") String pdUserId) {
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "PdUserId parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }

        try {
            //Set userId from token
            String userId = authHelper.getUserId();
            Donation donation = donationService.createTreesDonationTransaction(userId, trees, pdUserId);

            sendNotificationService.sendDonateTreesNotification(donation);

            return ResponseEntity.ok().body(new DonationResponse(null, donation));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientTreesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        }
        catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.DONATE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @PostMapping(value = "/donateLeafs")
    public ResponseEntity<?> donateLeafs(@RequestParam(value = "leafs") BigDecimal leafs, @RequestParam(value = "pdUserId") String pdUserId) {
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "PdUserId parameter is required."));
        }
        if (leafs == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafs parameter is required."));
        }

        try {
            //Set userId from token
            String userId = authHelper.getUserId();
            Donation donation = donationService.createLeafsDonationTransaction(userId, leafs, pdUserId);
            return ResponseEntity.ok().body(new DonationResponse(null, donation));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientTreesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.DONATE, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/donationHistoryForUser")
    public ResponseEntity<?> getDonationHistoryForUser(@RequestParam(value = "donorUserId", required = false) String donorUserId) {
        if (donorUserId == null || donorUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            List<Donation> userDonationHistory = donationService.userDonationHistory(donorUserId);

            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, userDonationHistory));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.DONATION_HISTORY_FOR_USER, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/donationHistoryForPd")
    public ResponseEntity<?> getDonationHistoryForPd(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size) {
        try {
            String pdUserId = authHelper.getUserId();
            Page<DonationHistoryResponse> userDonationHistory = donationService.pdDonationHistory(pdUserId, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, userDonationHistory));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.DONATION_HISTORY_FOR_PD, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/donationHistoryWithVideoStatsForPd")
    public ResponseEntity<?> getDonationHistoryWithVideoStatsForPd(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                                   @RequestParam(defaultValue = "10") @Min(1) int size) {
        try {
            String pdUserId = authHelper.getUserId();
            Page<DonationHistoryWithVideoStatsResponse> userDonationHistory = donationService.pdDonationHistoryWithVideoStats(pdUserId, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, userDonationHistory));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.DONATION_HISTORY_FOR_PD, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/topDonorsList")
    public ResponseEntity<?> getDonationHistoryForPd(@RequestParam(value = "pdUserId") String pdUserId, @RequestParam(value = "limit") Long limit) {
        if (limit == null || limit <= 0 || limit > 30) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "limit parameter is invalid or not passed. Please pass limit between 1-30"));
        }
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId cannot be null or empty"));
        }
        try {
            //String pdUserId = authHelper.getUserId();
            List<PublicUserNet> publicUserNetList = donationService.getTopDonorsInfo(pdUserId, limit);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.TOP_DONOR_LIST, e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }

    @GetMapping(value = "/topDonorsList/v2")
    public ResponseEntity<?> getDonationHistoryForPdV2(@RequestParam(value = "pdUserId") String pdUserId,
                                                       Pageable pageable) {
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId cannot be null or empty"));
        }
        try {
            Page<DonorData> donorData = donationService.getTopDonorsInfoV2(pdUserId, pageable);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, donorData));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.TOP_DONOR_LIST, e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }

    @GetMapping(value = "/topDonorsListDownload")
    public Mono<ResponseEntity<String>> topDonorsListDownload(
            @RequestParam(required = false, value = "pdUserId") String pdUserId,
            @RequestParam(required = false, value = "email") String email,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletResponse response
    ) {
//        donationService.topDonorsListDownload(email, pdUserId, startDate, endDate, response);
//        return Mono.just(ResponseEntity.accepted()
//                .body("File generation started. You will receive an email when it's complete."));
        return Mono.fromCallable(() -> {
                    donationService.topDonorsListDownload(email, pdUserId, startDate, endDate, response);
                    return "File generation started. Check your downloads or email.";
                })
                .map(successMessage -> ResponseEntity.accepted()
                .body("File generation started. You will receive an email when it's complete."))
                .onErrorResume(ResponseStatusException.class, ex -> {
                    String message = "Error: " + ex.getReason();
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(message));
                })
                .onErrorResume(Exception.class, ex -> {
                    ex.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to generate file: " + ex.getMessage()));
                });
    }

}
