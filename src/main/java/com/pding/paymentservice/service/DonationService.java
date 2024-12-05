package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.InvalidUserException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.other.services.tables.dto.DonorData;
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
import com.pding.paymentservice.util.LogSanitizer;
import com.pding.paymentservice.util.StringUtil;
import com.pding.paymentservice.util.TokenSigner;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
        log.info("start donation transaction for userId: {}, trees: {}, pdUserId: {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(treesToDonate), LogSanitizer.sanitizeForLog(PdUserId));
        if(otherServicesTablesNativeQueryRepository.findUserInfoByUserId(PdUserId).isEmpty()){
            log.error("PD User ID doesn't exist, {}", LogSanitizer.sanitizeForLog(PdUserId));
            throw new InvalidUserException("PD User ID doesn't exist");
        }

        walletService.deductTreesFromWallet(userId, treesToDonate);

        Donation transaction = new Donation(userId, PdUserId, treesToDonate, null);
        Donation donation = donationRepository.save(transaction);

        earningService.addTreesToEarning(PdUserId, treesToDonate);
        ledgerService.saveToLedger(donation.getId(), treesToDonate, new BigDecimal(0), TransactionType.DONATION, userId);
        log.info("Donation transaction completed for userId: {}, trees: {}, pdUserId: {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(treesToDonate), LogSanitizer.sanitizeForLog(PdUserId));

        return donation;
    }

    @Transactional
    public Donation createLeafsDonationTransaction(String userId, BigDecimal leafsToDonate, String PdUserId) {
        log.info("start donation transaction for userId: {}, leafs: {}, pdUserId: {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(leafsToDonate), LogSanitizer.sanitizeForLog(PdUserId));

        walletService.deductLeafsFromWallet(userId, leafsToDonate);

        Donation transaction = new Donation(userId, PdUserId, null, leafsToDonate);
        Donation donation = donationRepository.save(transaction);

        earningService.addLeafsToEarning(PdUserId, leafsToDonate);
        ledgerService.saveToLedger(donation.getId(), new BigDecimal(0), leafsToDonate, TransactionType.DONATION, userId);
        log.info("Donation transaction completed for userId: {}, leafs: {}, pdUserId: {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(leafsToDonate), LogSanitizer.sanitizeForLog(PdUserId));

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

    public Page<DonationHistoryWithVideoStatsResponse> pdDonationHistoryWithVideoStats(String pdUserId, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_update_date").ascending());

        List<DonationHistoryWithVideoStatsResponse> donationList = new ArrayList<>();
        Long totalVideosUploadedByUser = donationRepository.countTotalVideosUploadedByPdUserId(pdUserId);
        Page<Object[]> donationPage = donationRepository.findDonationHistoryWithVideoStatsByPdUserId(pdUserId, pageable);

        List<String> donorUserIds = donationPage.getContent().stream()
                .map(row -> (String) row[5])
                .collect(Collectors.toList());

        List<PublicUserNet> publicUsers = userServiceNetworkManager
            .getUsersListFlux(donorUserIds)
            .collect(Collectors.toList())
            .block();

        for (Object innerObject : donationPage.getContent()) {

            Object[] giftDonationHistory = (Object[]) innerObject;

            DonationHistoryWithVideoStatsResponse donation = new DonationHistoryWithVideoStatsResponse();
            donation.setUserEmailId(giftDonationHistory[0].toString());
            donation.setDonatedTrees(giftDonationHistory[1].toString());
            donation.setLastUpdateDate(giftDonationHistory[2].toString());
            donation.setTotalVideosWatchedByUser(giftDonationHistory[3].toString());
            donation.setTotalVideosUploadedByPD(totalVideosUploadedByUser.toString());
            donation.setRecentDonation(giftDonationHistory[4].toString());
            donation.setUserId(giftDonationHistory[5].toString());
            PublicUserNet user = publicUsers.stream().filter(u -> u.getId().equals(donation.getUserId())).findFirst().orElse(null);
            donation.setProfilePicture(tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getProfilePicture()), 8));
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

    public Page<DonorData> getTopDonorsInfoV2(String pdUserId, Pageable pageable) throws Exception {
        Page<Object[]> donorUserObjects = donationRepository.findTopDonorUser(pdUserId, pageable);

        if (donorUserObjects.isEmpty()) {
            return Page.empty();
        }

        List<String> donorUserIds = donorUserObjects.stream()
            .map(row -> (String) row[0])
            .collect(Collectors.toList());

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
        }


        Map<String, PublicUserNet> publicUserMap = publicUsers.stream()
            .collect(Collectors.toMap(PublicUserNet::getId, user -> user));


        Page<DonorData> donorDataPage = donorUserObjects.map(objects -> {
            DonorData donorData = new DonorData();
            donorData.setDonorUserId((String) objects[0]);
            donorData.setTotalTreeDonation((BigDecimal) objects[1]);
            donorData.setTotalPurchasedVideoTree((BigDecimal) objects[2]);
            Timestamp lastPurchasedVideoDate = (Timestamp) objects[3];
            Timestamp lastDonationDate = (Timestamp) objects[4];

            if (lastPurchasedVideoDate != null && lastDonationDate != null) {
                donorData.setLastUsedDate(lastPurchasedVideoDate.after(lastDonationDate) ? lastPurchasedVideoDate.toLocalDateTime() : lastDonationDate.toLocalDateTime());
            } else if (lastPurchasedVideoDate != null) {
                donorData.setLastUsedDate(lastPurchasedVideoDate.toLocalDateTime());
            } else if (lastDonationDate != null) {
                donorData.setLastUsedDate(lastDonationDate.toLocalDateTime());
            }

            donorData.setEmail(publicUserMap.get(donorData.getDonorUserId()).getEmail());
            donorData.setProfilePicture(publicUserMap.get(donorData.getDonorUserId()).getProfilePicture());
            donorData.setNickname(publicUserMap.get(donorData.getDonorUserId()).getNickname());

            return donorData;
        });

        return donorDataPage;
    }

    public ResponseEntity<?> donateToPd(String userId, BigDecimal trees, String pdUserId) {
        log.info("Donation request received for userId: {}, trees: {}, pdUserId: {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(trees), LogSanitizer.sanitizeForLog(pdUserId));
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

    public ResponseEntity<List<DonorData>> topDonorsListDownload(LocalDate startDate, LocalDate endDate) throws Exception{
        String userId = authHelper.getUserId();

        if (userId == null) {
            throw new IllegalArgumentException("UserId null; cannot get video sales history.");
        }

        if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
            throw new IllegalArgumentException("Both start date and end date should either be null or have a value");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Both start date and end date cannot be null at the same time");
        }
        endDate = endDate.plusDays(1L);

        List<Object[]> donorUserObjects = donationRepository.findTopDonorUserByDateRanger(userId, startDate,endDate);

        if (donorUserObjects.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<String> donorUserIds = donorUserObjects.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());

        List<PublicUserNet> publicUsers = userServiceNetworkManager
                .getUsersListFlux(donorUserIds)
                .collect(Collectors.toList())
                .block();

        if (publicUsers == null || publicUsers.isEmpty()) {
            // Optionally log this condition
            return ResponseEntity.ok(Collections.emptyList());
        }

        Map<String, PublicUserNet> publicUserMap = publicUsers.stream()
                .collect(Collectors.toMap(PublicUserNet::getId, user -> user));


        List<DonorData> donorDataList = donorUserObjects.stream().map(objects -> {
            DonorData donorData = new DonorData();
            donorData.setDonorUserId((String) objects[0]);
            donorData.setTotalTreeDonation((BigDecimal) objects[1]);
            donorData.setTotalPurchasedVideoTree((BigDecimal) objects[2]);
            Timestamp lastPurchasedVideoDate = (Timestamp) objects[3];
            Timestamp lastDonationDate = (Timestamp) objects[4];

            if (lastPurchasedVideoDate != null && lastDonationDate != null) {
                donorData.setLastUsedDate(lastPurchasedVideoDate.after(lastDonationDate)
                        ? lastPurchasedVideoDate.toLocalDateTime() : lastDonationDate.toLocalDateTime());
            } else if (lastPurchasedVideoDate != null) {
                donorData.setLastUsedDate(lastPurchasedVideoDate.toLocalDateTime());
            } else if (lastDonationDate != null) {
                donorData.setLastUsedDate(lastDonationDate.toLocalDateTime());
            }

            // Setting other donor details
            PublicUserNet user = publicUserMap.get(donorData.getDonorUserId());
            if (user != null) {
                donorData.setEmail(StringUtil.maskEmail(user.getEmail()));
                donorData.setNickname(user.getNickname());
            }

            return donorData;
        }).collect(Collectors.toList());

        // Return the list of donor data
        return ResponseEntity.ok(donorDataList);
    }
}
