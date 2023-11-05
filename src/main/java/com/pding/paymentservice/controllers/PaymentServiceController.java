package com.pding.paymentservice.controllers;

import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.VideoTransactions;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import com.pding.paymentservice.payload.response.BuyVideoResponse;
import com.pding.paymentservice.payload.response.ChargeResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GetVideoTransactionsResponse;
import com.pding.paymentservice.payload.response.MessageResponse;
import com.pding.paymentservice.payload.response.WalletHistoryResponse;
import com.pding.paymentservice.payload.response.WalletResponse;
import com.pding.paymentservice.repository.TotalTreesEarnedResponse;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.service.VideoTransactionsService;
import com.pding.paymentservice.service.WalletHistoryService;
import com.pding.paymentservice.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PaymentServiceController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    WalletService walletService;

    @Autowired
    WalletHistoryService walletHistoryService;

    @Autowired
    VideoTransactionsService videoTransactionsService;

    @GetMapping(value = "/test")
    public ResponseEntity<?> sampleGet() {
        return ResponseEntity.ok()
                .body(new MessageResponse("This is test API, With JWT Validation"));
    }

    @PostMapping("/charge")
    public ResponseEntity<?> chargeCard(@Valid @RequestBody PaymentDetailsRequest paymentDetailsRequest, BindingResult result, HttpServletRequest request) throws Exception {
        // Check for validation errors
        if (result.hasErrors()) {
            // Here, we're just grabbing the first error, but you might want to send all of them.
            ObjectError error = result.getAllErrors().get(0);
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error.getDefaultMessage())
            );
        }

        try {

            if (!paymentDetailsRequest.getTransactionStatus().equals("success")) {
                paymentDetailsRequest.setTrees(new BigDecimal(0));
            }
            String charge = paymentService.chargeCustomer(paymentDetailsRequest.getUserid(), paymentDetailsRequest.getTrees(),
                    paymentDetailsRequest.getPurchasedDate(), paymentDetailsRequest.getTransactionID(),
                    paymentDetailsRequest.getTransactionStatus(), paymentDetailsRequest.getAmount(),
                    paymentDetailsRequest.getPaymentMethod(), paymentDetailsRequest.getCurrency(),
                    paymentDetailsRequest.getDescription(), paymentDetailsRequest.getIpAddress());

            return ResponseEntity.ok().body(new ChargeResponse(null, charge));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ChargeResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/wallet")
    public ResponseEntity<?> getWallet(@RequestParam(value = "userid") Long userid, HttpServletRequest request) {
        if (userid == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            Optional<Wallet> wallet = walletService.fetchWalletByUserID(userid);
            return ResponseEntity.ok().body(new WalletResponse(null, wallet.get()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WalletResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/wallethistory")
    public ResponseEntity<?> getWallethistory(@RequestParam(value = "userid") Long userid, HttpServletRequest request) {
        if (userid == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            List<WalletHistory> walletHistory = walletHistoryService.fetchWalletHistoryByUserID(userid);
            return ResponseEntity.ok().body(new WalletHistoryResponse(null, walletHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WalletHistoryResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    @PostMapping(value = "/buyvideo")
    public ResponseEntity<?> buyVideo(@RequestParam(value = "userid") Long userid, @RequestParam(value = "contentid") Long contentid, @RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "videoOwnerUserID") Long videoOwnerUserID, HttpServletRequest request) {
        if (userid == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        if (contentid == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "contentid parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        try {
            VideoTransactions video = videoTransactionsService.createVideoTransaction(userid, contentid, trees, videoOwnerUserID);
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

    @GetMapping(value = "/videotransactions")
    public ResponseEntity<?> getVideotransactions(@RequestParam(value = "userid") Long userid, HttpServletRequest request) {
        if (userid == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            List<VideoTransactions> videoTransactions = videoTransactionsService.getAllTransactionsForUser(userid);
            return ResponseEntity.ok().body(new GetVideoTransactionsResponse(null, videoTransactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GetVideoTransactionsResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/treesEarned")
    public ResponseEntity<?> getTotalTreesEarnedByVideoOwner(@RequestParam(value = "videoOwnerUserID") Long videoOwnerUserID) {
        if (videoOwnerUserID == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            BigDecimal videoTransactions = videoTransactionsService.getTotalTreesEarnedByVideoOwner(videoOwnerUserID);
            return ResponseEntity.ok().body(new TotalTreesEarnedResponse(null, videoTransactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TotalTreesEarnedResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
