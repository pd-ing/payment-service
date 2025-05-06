package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.RefundVideoPurchaseRequest;
import com.pding.paymentservice.payload.request.VideoPurchaseStatusRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GetVideoTransactionsResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.VideoPurchaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class VideoPurchaseServiceController {

    @Autowired
    AuthHelper authHelper;
    @Autowired
    VideoPurchaseService videoPurchaseService;
    @Autowired
    PdLogger pdLogger;

    @PostMapping(value = "/v3/buyVideo")
    public ResponseEntity<?> buyVideoV3(@RequestParam(value = "videoId") String videoId, @RequestParam("duration") String duration) {
        return videoPurchaseService.buyVideoV3(videoId, duration);
    }

    @PostMapping(value = "/videoPurchaseTimeRemaining")
    public ResponseEntity<?> getVideoPurchaseTimeRemaining(@RequestBody VideoPurchaseStatusRequest request) {
        return videoPurchaseService.getVideoPurchaseTimeRemaining(request.getUserId(), request.getVideoIds());
    }

    @GetMapping(value = "/videoPurchaseHistory")
    public ResponseEntity<?> getVideotransactions(@RequestParam(value = "userId", required = false) String userId, HttpServletRequest request) {
        return videoPurchaseService.getVideoTransactions(authHelper.getUserId());
    }

    @GetMapping(value = "/videoPurchaseHistoryOfUser")
    public ResponseEntity<?> getVideoTransactionsPageable(
            @RequestParam(value = "creatorUserId", required = false) String creatorUserId,
            @RequestParam(value = "sort", defaultValue = "1") int sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int pageSize
    ) {
        String userId = authHelper.getUserId();
        return ResponseEntity.ok().body(videoPurchaseService.getVideoTransactions(userId, creatorUserId, page, pageSize, sort));
    }

    @GetMapping(value = "/expiredVideoPurchases")
    public ResponseEntity<?> getExpiredVideoPurchases(
            @RequestParam(value = "creatorUserId", required = false) String creatorUserId,
            @RequestParam(value = "sort", defaultValue = "1") int sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int pageSize
    ) {
        String userId = authHelper.getUserId();
        GetVideoTransactionsResponse result = videoPurchaseService.expiredVideoPurchases(userId, creatorUserId, page, pageSize, sort);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(value = "/isVideoPurchased")
    public ResponseEntity<?> isVideoPurchasedByUser(@RequestParam(value = "userId", required = false) String userId, @RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchased(authHelper.getUserId(), videoId);
    }

    @GetMapping(value = "/v2/isVideoPurchased")
    public ResponseEntity<?> getVideoPurchasedStatus(@RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchasedV2(authHelper.getUserId(), videoId);
    }

    @GetMapping(value = "/paidUnpaidFollowerCount")
    public ResponseEntity<?> getPaidUnpaidFollowerCount(@RequestParam(value = "userId", required = false) String userId)
    {
        return videoPurchaseService.getPaidUnpaidFollowerCount(authHelper.getUserId());
    }


    @GetMapping(value = "/videoEarningAndSales")
    public ResponseEntity<?> videoEarningAndSales(@RequestParam(value = "videoIds") String videoIds) {
        return videoPurchaseService.videoEarningAndSales(videoIds);
    }

//    @PostMapping(value = "/videoPurchaseReplacement")
//    public ResponseEntity<?> videoPurchaseReplacement(
//            @RequestParam(value = "videoId") String videoId,
//            @RequestParam(value = "ownerUserId", required = false) String ownerUserId,
//            @RequestParam(value = "userEmails") String userEmails
//    ) {
//        return videoPurchaseService.createVideoPurchaseReplacementFromEmail(videoId, ownerUserId, userEmails);
//    }

    @GetMapping(value = "/getAllPdWhoseVideosArePurchasedByUser")
    public ResponseEntity<?> getAllPdUserIdWhoseVideosArePurchasedByUser(
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(required = false) String searchString
    ) {
        return videoPurchaseService.getAllPdUserIdWhoseVideosArePurchasedByUser(searchString, size, page);
    }

    @GetMapping(value = "/getAllPdWhoseVideosAreExpiredByUser")
    public ResponseEntity<?> getAllPdWhoseVideosAreExpiredByUser(
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(required = false) String searchString
    ) {
        return videoPurchaseService.getAllPdWhoseVideosAreExpiredByUser(searchString, size, page);
    }

    @GetMapping(value = "/videoSalesHistoryOfPd")
    public ResponseEntity<?> getVideoSalesHistoryOfUser(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size,
            @RequestParam(value = "searchString", required = false) String searchString
    ) {
        if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
        }
        if (endDate != null) {
            endDate = endDate.plusDays(1L);
        }
        if(StringUtils.isBlank(searchString)) {
            searchString = null;
        }
        return videoPurchaseService.getSalesHistoryOfUser(searchString, startDate, endDate, page, size, sortOrder);
    }

//    @GetMapping(value = "/searchVideoSalesHistoryOfPd")
//    public ResponseEntity<?> searchVideoSalesHistoryOfUser(@RequestParam(value = "searchString") @NotBlank String searchString,
//                                                           @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
//                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
//                                                           @RequestParam(defaultValue = "10") @Min(1) int size) {
//        return videoPurchaseService.searchSalesHistoryOfUser(searchString, page, size, sortOrder);
//    }

    @GetMapping(value = "/dailyTreeRevenueOfPd")
    public ResponseEntity<?> getDailyTreeRevenueOfUser(@RequestParam(value = "endDate") LocalDateTime endDate) {
        return videoPurchaseService.getDailyTreeRevenueOfUser(authHelper.getUserId(), endDate);
    }


    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }

//    @GetMapping(value = "/salesHistoryDownloadPreparing")
//    public Flux<ServerSentEvent<GenerateReportEvent>> salesHistoryDownloadPreparing(
//            @RequestParam(required = false, value = "email") String email,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
//            @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
//            @RequestParam(value = "searchString", required = false) String searchString
//    ){
//        // Call service to process export
//        String pdUserId = authHelper.getUserId();
//        return videoPurchaseService.salesHistoryDownloadPreparing(pdUserId, email, searchString, startDate, endDate, sortOrder)
//                .map(event -> ServerSentEvent.<GenerateReportEvent>builder()
//                        .id(event.getReportId())
//                        .event(event.getEventType())
//                        .data(event)
//                        .build())
//                .doOnNext(sse -> pdLogger.logInfo("Emitting SSE: {}", sse.event()))
//                .doOnError(error -> pdLogger.logInfo("Error in report generation", error.toString()));
//    }

    @GetMapping(value = "/salesHistoryDownload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> salesHistoryDownload(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
            @RequestParam(value = "searchString", required = false) String searchString,
            @RequestParam(required = false, value = "isSendEmail", defaultValue = "false") Boolean isSendEmail,
            HttpServletResponse httpServletResponse
    ){
        try {
            String pdUserId = authHelper.getUserId();
            return videoPurchaseService.salesHistoryDownloadPDF(pdUserId, searchString, startDate, endDate, sortOrder, isSendEmail,httpServletResponse);
        } catch (ResponseStatusException e) {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(responseMap);
        } catch (Exception e) {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(responseMap);
        }
    }

    @PostMapping("/admin/refund-video-purchase")
    public ResponseEntity<?> refundVideo(@RequestBody RefundVideoPurchaseRequest request) throws Exception {
        return videoPurchaseService.refundVideoPurchase(request.getTransactionId());
    }


}
