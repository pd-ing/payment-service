package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.InvalidUserException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.response.DonationResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.DonationHistoryForAdminDashboard;
import com.pding.paymentservice.payload.response.donation.DonationHistoryResponse;
import com.pding.paymentservice.payload.response.donation.DonationHistoryWithVideoStatsResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.repository.DonationRepository;

import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.TokenSigner;
import lombok.extern.slf4j.Slf4j;

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
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Autowired
    AuthHelper authHelper;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;
    

    @Transactional
    public Donation createTreesDonationTransaction(String userId, BigDecimal treesToDonate, String PdUserId) {
        if(otherServicesTablesNativeQueryRepository.findUserInfoByUserId(PdUserId).isEmpty()){
            throw new InvalidUserException("PD User ID doesn't exist");
        }

        walletService.deductTreesFromWallet(userId, treesToDonate);

        Donation transaction = new Donation(userId, PdUserId, treesToDonate, null);
        Donation donation = donationRepository.save(transaction);

        earningService.addTreesToEarning(PdUserId, treesToDonate);
        ledgerService.saveToLedger(donation.getId(), treesToDonate, new BigDecimal(0), TransactionType.DONATION, userId);

        return donation;
    }

    @Transactional
    public Donation createLeafsDonationTransaction(String userId, BigDecimal leafsToDonate, String PdUserId) {

        walletService.deductLeafsFromWallet(userId, leafsToDonate);

        Donation transaction = new Donation(userId, PdUserId, null, leafsToDonate);
        Donation donation = donationRepository.save(transaction);

        earningService.addLeafsToEarning(PdUserId, leafsToDonate);
        ledgerService.saveToLedger(donation.getId(), new BigDecimal(0), leafsToDonate, TransactionType.DONATION, userId);

        return donation;
    }


    public List<Donation> userDonationHistory(String userId) {
        return donationRepository.findByDonorUserId(userId);
    }

    public Page<DonationHistoryResponse> pdDonationHistory(String pdUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_update_date").ascending());

        List<DonationHistoryResponse> donationList = new ArrayList<>();
        Page<Object[]> donationPage = donationRepository.findByPdUserId(pdUserId, pageable);
        for (Object innerObject : donationPage.getContent()) {

            Object[] giftDonationHistory = (Object[]) innerObject;

            DonationHistoryResponse donation = new DonationHistoryResponse();
            donation.setUserEmailId(giftDonationHistory[0].toString());
            donation.setDonatedTrees(giftDonationHistory[1].toString());
            donation.setLastUpdateDate(giftDonationHistory[2].toString());

            donationList.add(donation);
        }
        return new PageImpl<>(donationList, pageable, donationPage.getTotalElements());
    }

    public Page<DonationHistoryWithVideoStatsResponse> pdDonationHistoryWithVideoStats(String pdUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_update_date").ascending());

        List<DonationHistoryWithVideoStatsResponse> donationList = new ArrayList<>();
        Long totalVideosUploadedByUser = donationRepository.countTotalVideosUploadedByPdUserId(pdUserId);
        Page<Object[]> donationPage = donationRepository.findDonationHistoryWithVideoStatsByPdUserId(pdUserId, pageable);

        for (Object innerObject : donationPage.getContent()) {

            Object[] giftDonationHistory = (Object[]) innerObject;

            DonationHistoryWithVideoStatsResponse donation = new DonationHistoryWithVideoStatsResponse();
            donation.setUserEmailId(giftDonationHistory[0].toString());
            donation.setDonatedTrees(giftDonationHistory[1].toString());
            donation.setLastUpdateDate(giftDonationHistory[2].toString());
            donation.setTotalVideosWatchedByUser(giftDonationHistory[3].toString());
            donation.setTotalVideosUploadedByPD(totalVideosUploadedByUser.toString());
            donation.setRecentDonation(giftDonationHistory[4].toString());

            donationList.add(donation);
        }
        return new PageImpl<>(donationList, pageable, donationPage.getTotalElements());
    }

    public List<PublicUserNet> getTopDonorsInfo(String pdUserId, Long limit) throws Exception {
        List<Object[]> donorUserObjects = donationRepository.findTopDonorUserAndDonatedTreesByPdUserID(pdUserId, limit);
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
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "userid parameter is required."));
        }
        if (pdUserId == null || pdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "PdUserId parameter is required."));
        }
        if (trees == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }

        try {
            Donation donation = createTreesDonationTransaction(userId, trees, pdUserId);
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

}
