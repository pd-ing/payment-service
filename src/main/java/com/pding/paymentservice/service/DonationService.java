package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.TransactionType;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.response.DonationResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.GenericListDataResponse;
import com.pding.paymentservice.repository.DonationRepository;

import com.pding.paymentservice.util.TokenSigner;
import lombok.extern.slf4j.Slf4j;

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

    @Autowired
    UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    TokenSigner tokenSigner;

    @Autowired
    PdLogger pdLogger;

    @Transactional
    public Donation createDonationTransaction(String userId, BigDecimal treesToDonate, String PdUserId) {
        walletService.deductFromWallet(userId, treesToDonate);

        Donation transaction = new Donation(userId, PdUserId, treesToDonate);
        Donation donation = donationRepository.save(transaction);

        earningService.addToEarning(PdUserId, treesToDonate);
        ledgerService.saveToLedger(donation.getId(), treesToDonate, TransactionType.DONATION);

        return donation;
    }


    public List<Donation> userDonationHistory(String userId) {
        return donationRepository.findByDonorUserId(userId);
    }

    public List<Donation> pdDonationHistory(String pdUserId) {
        return donationRepository.findByPdUserId(pdUserId);
    }

    public List<PublicUserNet> getTopDonorsInfo(Long limit) throws Exception {
        List<Object[]> donorUserObjects = donationRepository.findTopDonorUserIds(limit);
        if (donorUserObjects.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> donorUserIds = donorUserObjects.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());


        Map<String, BigDecimal> topDonorsMap = donorUserObjects.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],          // donorUserId
                        row -> (BigDecimal) row[1]       // totalDonatedTrees
                ));

        List<PublicUserNet> publicUsers = userServiceNetworkManager
                .getUsersListFlux(donorUserIds)
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

            BigDecimal treesDonated = topDonorsMap.get(user.getId());
            
            user.setTreesDonated(treesDonated);
            user.setProfilePicture(profilePicture);
            user.setCoverImage(coverImage);
        }

        return publicUsers;
    }

    public ResponseEntity<?> donateToPd(String userId, BigDecimal trees, String pdUserId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        if (pdUserId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "PdUserId parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        try {
            Donation donation = createDonationTransaction(userId, trees, pdUserId);
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

    public ResponseEntity<?> getDonationHistoryForUser(String userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        try {
            List<Donation> userDonationHistory = userDonationHistory(userId);

            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, userDonationHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getDonationHistoryForPd(String pdUserId) {
        if (pdUserId == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }
        try {
            List<Donation> userDonationHistory = pdDonationHistory(pdUserId);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, userDonationHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DonationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getTopDonors(Long limit) {
        if (limit == null || limit <= 0 || limit > 30) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "limit parameter is invalid or not passed. Please pass limit between 1-30"));
        }
        try {
            List<PublicUserNet> publicUserNetList = getTopDonorsInfo(limit);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }
}
