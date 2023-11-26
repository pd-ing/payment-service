package com.pding.paymentservice.service;

import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.TransactionType;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.response.BuyVideoResponse;
import com.pding.paymentservice.payload.response.DonationResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.repository.DonationRepository;
import com.pding.paymentservice.repository.EarningRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
public class DonationService {

    @Autowired
    DonationRepository donationRepository;

    @Autowired
    WalletService walletService;

    @Autowired
    EarningService earningService;

    @Autowired
    LedgerService ledgerService;

    @Transactional
    public Donation createDonationTransaction(String userId, BigDecimal treesToDonate, String PdUserId) {
        walletService.deductFromWallet(userId, treesToDonate);

        Donation transaction = new Donation(userId, PdUserId, treesToDonate);
        Donation donation = donationRepository.save(transaction);

        earningService.addToEarning(PdUserId, treesToDonate);
        ledgerService.saveToLedger(donation.getId(), treesToDonate, TransactionType.DONATION);

        return donation;
    }

    public ResponseEntity<?> donateToPd(String userId, BigDecimal trees, String PdUserId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        if (PdUserId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "PdUserId parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        try {
            Donation donation = createDonationTransaction(userId, trees, PdUserId);
            return ResponseEntity.ok().body(new DonationResponse(null, donation));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        } catch (InsufficientTreesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (InvalidAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DonationResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }
}
