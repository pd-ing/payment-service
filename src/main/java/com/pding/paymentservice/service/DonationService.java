package com.pding.paymentservice.service;


import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.InvalidUserException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.other.services.tables.dto.DonorData;
import com.pding.paymentservice.models.report.*;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.response.DonationResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.donation.DonationHistoryResponse;
import com.pding.paymentservice.payload.response.donation.DonationHistoryWithVideoStatsResponse;
import com.pding.paymentservice.repository.DonationRepository;

import com.pding.paymentservice.repository.GenerateReportEvent;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.DateTimeUtil;
import com.pding.paymentservice.util.LogSanitizer;
import com.pding.paymentservice.util.StringUtil;
import com.pding.paymentservice.util.TokenSigner;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
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

    @Autowired
    PDFService pdfService;

    @Autowired
    EmailSenderService emailSenderService;

    @Transactional
    public Donation createTreesDonationTransaction(String userId, BigDecimal treesToDonate, String PdUserId) {
        log.info("start donation transaction for userId: {}, trees: {}, pdUserId: {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(treesToDonate), LogSanitizer.sanitizeForLog(PdUserId));
        if (otherServicesTablesNativeQueryRepository.findUserInfoByUserId(PdUserId).isEmpty()) {
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

//    @Cacheable(value = "top_donations", key = "{#pdUserId, #limit}", cacheManager = "cacheManager")
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

    public Page<DonorData> getTopDonorsInfoV2(String pdUserId, String searchString, Pageable pageable) throws Exception {
        Page<Object[]> donorUserObjects = donationRepository.findTopDonorUser(pdUserId, searchString, pageable);

        if (donorUserObjects.isEmpty()) {
            return Page.empty();
        }

        List<String> donorUserIds = donorUserObjects.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());

//        List<PublicUserNet> publicUsers = userServiceNetworkManager
//                .getUsersListFlux(donorUserIds)
//                .collect(Collectors.toList())
//                .block();

//        for (PublicUserNet user : publicUsers) {
//            String profilePicture = null;
//            try {
//                if (user.getProfilePicture() != null) {
//                    profilePicture = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getProfilePicture()), 8);
//                }
//            } catch (Exception e) {
//                pdLogger.logException(PdLogger.EVENT.IMAGE_CDN_LINK, e);
//                e.printStackTrace();
//
//            }
//        }


//        Map<String, PublicUserNet> publicUserMap = publicUsers.stream()
//                .collect(Collectors.toMap(PublicUserNet::getId, user -> user));


        Page<DonorData> donorDataPage = donorUserObjects.map(objects -> {
            DonorData donorData = new DonorData();
            donorData.setDonorUserId((String) objects[0]);
            donorData.setTotalPurchasedVideoTree((BigDecimal) objects[1]);
            Timestamp lastPurchasedVideoDate = (Timestamp) objects[2];
            donorData.setTotalTreeDonation((BigDecimal) objects[3]);
            Timestamp lastDonationDate = (Timestamp) objects[4];

            if (lastPurchasedVideoDate != null && lastDonationDate != null) {
                donorData.setLastUsedDate(lastPurchasedVideoDate.after(lastDonationDate) ? lastPurchasedVideoDate.toLocalDateTime() : lastDonationDate.toLocalDateTime());
            } else if (lastPurchasedVideoDate != null) {
                donorData.setLastUsedDate(lastPurchasedVideoDate.toLocalDateTime());
            } else if (lastDonationDate != null) {
                donorData.setLastUsedDate(lastDonationDate.toLocalDateTime());
            }

//            donorData.setEmail(publicUserMap.get(donorData.getDonorUserId()).getEmail());
//            donorData.setProfilePicture(tokenSigner.signImageUrl(tokenSigner.composeImagesPath(publicUserMap.get(donorData.getDonorUserId()).getProfilePicture()), 8));
//            donorData.setNickname(publicUserMap.get(donorData.getDonorUserId()).getNickname());
//            donorData.setIsCreator(publicUserMap.get(donorData.getDonorUserId()).getIsCreator());

            donorData.setEmail((String) objects[5]);
            donorData.setProfilePicture(tokenSigner.signImageUrl(tokenSigner.composeImagesPath((String) objects[6]), 8));
            donorData.setNickname((String) objects[7]);
            donorData.setIsCreator(objects[8] != null ? (Boolean) objects[8]: false);

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

    public Flux<GenerateReportEvent> topDonorsListDownloadPreparing(String userId, String email, LocalDate startDate, LocalDate endDate ) {
        String reportId = UUID.randomUUID().toString();
        Flux<GenerateReportEvent> reportGenerationEvents;
        // Start generate report event
        reportGenerationEvents = topDonorsListStream(reportId, email, userId, startDate, endDate);

        return reportGenerationEvents
                .doOnNext(event -> {
                    // handle event
                    if (event instanceof ReportGenerationStartedEvent) {
                        log.info("Report generation started: " + event.getReportId());
                    } else if (event instanceof ReportGenerationInProgressEvent) {
                        log.info("Report is in progress: " + event.getReportId());
                    } else if (event instanceof ReportGenerationCompletedEvent) {
                        log.info("Report generation completed: " + event.getReportId());
                    } else if (event instanceof ReportGenerationFailedEvent) {
                        log.error("Report generation failed: " + event.getReportId());
                    }
                });
    }
    private Flux<GenerateReportEvent> topDonorsListStream(String reportId, String email, String pdUserId, LocalDate startDate, LocalDate endDate) {
        return Flux.<GenerateReportEvent>create(emitter -> {
            try {
                // Step 1: Report Generation Started
                emitter.next(ReportGenerationStartedEvent.builder()
                        .reportId(reportId)
                        .reportType("Top Donors Report")
                        .parameters(Map.of("startDate", startDate, "endDate", endDate))
                        .timestamp(System.currentTimeMillis())
                        .build());

                // Determine the user ID
                String userId = pdUserId != null ? pdUserId : otherServicesTablesNativeQueryRepository.findUserIdByEmail(email);
                if (userId == null) {
                    emitter.error(new IllegalArgumentException("User ID or email must be provided."));
                    return;
                }

                // Query email and nickname for the user
                List<Object[]> userInfo = otherServicesTablesNativeQueryRepository.findEmailAndNicknameByUserId(userId);
                String nickname = (userInfo != null && !userInfo.isEmpty() && userInfo.get(0)[1] != null)
                        ? userInfo.get(0)[1].toString() : "";

                // Step 2: Fetch donor data
                getMonoTopDonorsList(userId, startDate, endDate)
                        .flatMapMany(Flux::fromIterable)
                        .collectList()
                        .flatMap(donorDataList -> {
                            if (donorDataList.isEmpty()) {
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No donor data available for the given criteria."));
                            }

                            // Step 3: Generate PDF
                            return Mono.fromCallable(() -> {
                                ByteArrayOutputStream pdfContent = pdfService.generateFilePDFDonation(donorDataList, userId, nickname);
                                if (pdfContent == null || pdfContent.size() == 0) {
                                    throw new IllegalStateException("Failed to generate PDF content.");
                                }
                                return pdfContent.toByteArray();
                            }).subscribeOn(Schedulers.boundedElastic());
                        })
                        .flatMap(pdfBytes -> {
                            // Step 4: Write PDF to response
                            return Mono.fromRunnable(() -> {
                                        try {
                                            pdfService.cachePdfContent(reportId,pdfBytes);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .then(Mono.just(pdfBytes));
                        })
                        .doOnNext(pdfBytes -> {
                            // Step 5: Emit completion event
                            emitter.next(ReportGenerationCompletedEvent.builder()
                                    .reportId(reportId)
                                    .reportTitle("Top Donors Report")
                                    .metadata(Map.of("message", "The report has been successfully generated and is ready for download.",
                                            "startDate", startDate, "endDate", endDate))
                                    .timestamp(System.currentTimeMillis())
                                    .build());
                        })
                        .doOnError(e -> {
                            // Emit error event
                            emitter.next(ReportGenerationFailedEvent.builder()
                                    .reportId(reportId)
                                    .errorCode("GEN-001")
                                    .errorMessage(e.getMessage())
                                    .failureStep("PROCESSING")
                                    .timestamp(System.currentTimeMillis())
                                    .errorDetails(Map.of("exception", e.getClass().getName()))
                                    .build());
                            emitter.error(e);
                        })
                        .doFinally(signalType -> {
                            if (signalType == SignalType.ON_COMPLETE) {
                                emitter.complete();
                            }
                        })
                        .subscribe();
            } catch (Exception e) {
                // Emit error if initialization fails
                emitter.next(ReportGenerationFailedEvent.builder()
                        .reportId(reportId)
                        .errorCode("GEN-002")
                        .errorMessage(e.getMessage())
                        .failureStep("INITIALIZATION")
                        .timestamp(System.currentTimeMillis())
                        .errorDetails(Map.of("exception", e.getClass().getName()))
                        .build());
                emitter.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER).subscribeOn(Schedulers.boundedElastic());
    }
    private Mono<List<DonorData>> getMonoTopDonorsList(String userId, LocalDate startDate, LocalDate endDate) throws Exception {
        if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
            return Mono.error(new IllegalArgumentException("Both start date and end date should either be null or have a value"));
        }
        if (startDate == null) {
            return Mono.error(new IllegalArgumentException("Both start date and end date cannot be null at the same time"));
        }

        endDate = endDate.plusDays(1L);

        List<Object[]> donorUserObjects = donationRepository.findTopDonorUserByDateRanger(userId, startDate, endDate);
        if (donorUserObjects.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        List<String> donorUserIds = donorUserObjects.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());

        return userServiceNetworkManager.getUsersListFlux(donorUserIds)
                .collectList()
                .map(publicUsers -> {
                    if (publicUsers == null || publicUsers.isEmpty()) {
                        return Collections.emptyList();
                    }

                    Map<String, PublicUserNet> publicUserMap = publicUsers.stream()
                            .collect(Collectors.toMap(PublicUserNet::getId, user -> user));

                    return donorUserObjects.stream().map(objects -> {
                        DonorData donorData = new DonorData();
                        donorData.setDonorUserId((String) objects[0]);
                        donorData.setTotalTreeDonation((BigDecimal) objects[1]);
                        donorData.setTotalPurchasedVideoTree((BigDecimal) objects[2]);
                        Timestamp lastPurchasedVideoDate = (Timestamp) objects[3];
                        Timestamp lastDonationDate = (Timestamp) objects[4];

                        if (lastPurchasedVideoDate != null && lastDonationDate != null) {
                            LocalDateTime lastPurchasedVideoDateTime = DateTimeUtil.convertToLocalDateTime(lastPurchasedVideoDate);
                            LocalDateTime lastDonationDateTime = DateTimeUtil.convertToLocalDateTime(lastDonationDate);

                            donorData.setLastUsedDate(
                                    lastPurchasedVideoDateTime.isAfter(lastDonationDateTime)
                                            ? lastPurchasedVideoDateTime
                                            : lastDonationDateTime
                            );
                        } else if (lastPurchasedVideoDate != null) {
                            donorData.setLastUsedDate(DateTimeUtil.convertToLocalDateTime(lastPurchasedVideoDate));
                        } else if (lastDonationDate != null) {
                            donorData.setLastUsedDate(DateTimeUtil.convertToLocalDateTime(lastDonationDate));
                        }
                        donorData.setLastUsedDateFormatted(DateTimeUtil.formatLocalDateTime(donorData.getLastUsedDate()));

                        // Setting other donor details
                        PublicUserNet user = publicUserMap.get(donorData.getDonorUserId());
                        if (user != null) {
                            donorData.setEmail(StringUtil.maskEmail(user.getEmail()));
                            donorData.setNickname(user.getNickname());
                        }

                        return donorData;
                    }).collect(Collectors.toList());
                });
    }
    public ResponseEntity<Map<String, String>> topDonorsListDownload(String reportId, Boolean isSendEmail,HttpServletResponse response) {
        boolean isPdfFetched = false;
        try {
            // Fetch the PDF from storage
            byte[] pdfBytes = pdfService.getPDF(reportId); // Custom service to fetch the PDF

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF not found for reportId: " + reportId);
            }
            isPdfFetched = true;
            String dateTimeNow = DateTimeUtil.getCurrentTimeNow();
            String fileName = "top_donors_report_"+dateTimeNow+".pdf";
            if (Boolean.TRUE.equals(isSendEmail)){
                String userId = authHelper.getUserId();
                Optional<String> email = otherServicesTablesNativeQueryRepository.findEmailByUserId(userId);
                if (email.isEmpty()) {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("Error", "Email is required when sending via email.");
                    return ResponseEntity.badRequest().body(responseMap);
                }
                emailSenderService.sendEmailWithAttachmentBytes(email.get(),"Your requested Donor Report is ready for download.", "PDF Report",fileName,pdfBytes);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "PDF has been sent to the email address.");
                return ResponseEntity.ok(responseMap);
            }else{
                // Return the PDF as a downloadable response
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename="+fileName);
                response.setContentLength(pdfBytes.length);

                response.getOutputStream().write(pdfBytes);
                response.getOutputStream().flush();
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "PDF is ready for download.");
                return ResponseEntity.ok(responseMap);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF not found for reportId: " + reportId, e);
        } finally {
          if(isPdfFetched) pdfService.deletePDF(reportId);
        }
    }

}
