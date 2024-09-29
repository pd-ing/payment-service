package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.InvalidTransactionIDException;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserWithStripeIdNet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.admin.userTabs.WithdrawHistoryForPd;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.WithdrawHistoryForAdminDashboard;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.custompagination.PaginationInfoWithGenericList;
import com.pding.paymentservice.payload.response.custompagination.PaginationResponse;
import com.pding.paymentservice.payload.response.WithdrawalResponseWithStripeId;
import com.pding.paymentservice.repository.WithdrawalRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.paymentclients.stripe.StripeClient;
import com.pding.paymentservice.util.TokenSigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@Slf4j
public class WithdrawalService {

    @Autowired
    WithdrawalRepository withdrawalRepository;

    @Autowired
    EarningService earningService;

    @Autowired
    ReferralCommissionService referralCommissionService;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    private StripeClient stripeClient;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    AuthHelper authHelper;

    @Autowired
    UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    TokenSigner tokenSigner;


    @Transactional
    public Withdrawal startWithdrawal(String pdUserId, BigDecimal trees, BigDecimal leafs) throws Exception {
        if (LocalDateTime.now().getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new Exception("Withdrawal requests can only be made on Mondays.");
        }

        List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, WithdrawalStatus.PENDING);
        if (!withdrawalList.isEmpty()) {
            throw new Exception("You already have an ongoing withdrawal request.");
        }

        earningService.deductTreesAndLeafs(pdUserId, trees, leafs);

        Withdrawal withdrawal = new Withdrawal(pdUserId, trees, leafs, WithdrawalStatus.PENDING);
        withdrawalRepository.save(withdrawal);

        referralCommissionService.createReferralCommissionEntryInPendingState(withdrawal);

        ledgerService.saveToLedger(withdrawal.getId(), trees, leafs, TransactionType.WITHDRAWAL_STARTED, pdUserId);

        log.info("Withdrawal request created for pdUserId {}, trees {}, leafs {}", pdUserId, trees, leafs);
        return withdrawal;
    }


    @Transactional
    public Withdrawal completeWithdrawal(String pdUserId) throws Exception {
        List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, WithdrawalStatus.PENDING);

        if (withdrawalList.size() == 1) {

            Withdrawal withdrawal = withdrawalList.get(0);
            withdrawal.setStatus(WithdrawalStatus.COMPLETE);
            withdrawalRepository.save(withdrawal);

            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), withdrawal.getLeafs(), TransactionType.WITHDRAWAL_COMPLETED, pdUserId);
            log.info("Withdrawal request completed for pdUserId {}, trees {}, leafs {}", pdUserId, withdrawal.getTrees(), withdrawal.getLeafs());
            return withdrawal;
        } else if (withdrawalList.size() > 1) {
            throw new Exception("More than 1 withdrawal request found in PENDING status for pdUserId " + pdUserId);
        } else {
            throw new Exception("Invalid transaction, Please start withdraw request before completing it ");
        }
    }

    @Transactional
    public void failWithdrawal(String pdUserId) throws Exception {
        List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, WithdrawalStatus.PENDING);

        if (withdrawalList.size() == 1) {

            Withdrawal withdrawal = withdrawalList.get(0);
            withdrawal.setStatus(WithdrawalStatus.FAILED);
            withdrawalRepository.save(withdrawal);

            earningService.addTreesAndLeafsToEarning(withdrawal.getPdUserId(), withdrawal.getTrees(), withdrawal.getLeafs());

            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), withdrawal.getLeafs(), TransactionType.WITHDRAWAL_FAILED, pdUserId);
            ledgerService.saveToLedger(withdrawal.getId(), withdrawal.getTrees(), new BigDecimal(0), TransactionType.TREES_REVERTED, pdUserId);
            ledgerService.saveToLedger(withdrawal.getId(), new BigDecimal(0), withdrawal.getLeafs(), TransactionType.LEAFS_REVERTED, pdUserId);

            log.info("Withdrawal request cancelled for pdUserId {}, trees {}, leafs {}", pdUserId, withdrawal.getTrees(), withdrawal.getLeafs());
        } else if (withdrawalList.size() > 1) {
            throw new Exception("More than 1 withdrawal request found in PENDING status for pdUserId " + pdUserId);
        } else {
            throw new Exception("Invalid transaction, Please start withdraw request before failing it ");
        }
    }

    private boolean validatePaymentIntentID(String pdUserId, String transactionId) throws Exception {
        if (!stripeClient.isPaymentIntentIDPresentInStripe(transactionId)) {
            throw new InvalidTransactionIDException("paymentIntent id : " + transactionId + " , is invalid");
        }

//        if (withdrawalRepository.findByPdUserIdAndTransactionId(pdUserId, transactionId).isPresent()) {
//            throw new InvalidTransactionIDException("paymentIntent id : " + transactionId + " , is already used for the payment");
//        }

        return true;
    }

    public List<String> extractPdUserIds(List<Withdrawal> withdrawalList) {
        return withdrawalList.stream()
                .map(Withdrawal::getPdUserId)
                .collect(Collectors.toList());
    }

    public Map<String, PublicUserWithStripeIdNet> createMapOfPublicUserWithStripeIdNet(List<PublicUserWithStripeIdNet> publicUserWithStripeIdNetList) {
        Map<String, PublicUserWithStripeIdNet> userMap = new HashMap<>();

        if (publicUserWithStripeIdNetList == null) {
            return userMap;
        }

        for (PublicUserWithStripeIdNet user : publicUserWithStripeIdNetList) {
            userMap.put(user.id, user);
        }

        return userMap;
    }

    List<WithdrawalResponseWithStripeId> createWithDrawResponseWithStripeId(List<Withdrawal> withdrawalList) throws Exception {
        List<String> withdrawalIds = extractPdUserIds(withdrawalList);
        List<PublicUserWithStripeIdNet> publicUserWithStripeIdNetList = userServiceNetworkManager.getUsersListWithStripeFlux(withdrawalIds).collect(Collectors.toList())
                .block();

        Map<String, PublicUserWithStripeIdNet> publicUserWithStripeIdNetMap = createMapOfPublicUserWithStripeIdNet(publicUserWithStripeIdNetList);

        // Creating a new List<WithdrawalResponseWithStripeId>
        List<WithdrawalResponseWithStripeId> responseList = new ArrayList<>();

        for (Withdrawal withdrawal : withdrawalList) {

            PublicUserWithStripeIdNet publicUserWithStripeIdNet = publicUserWithStripeIdNetMap.get(withdrawal.getPdUserId());

            String email = "";
            String nickname = "";
            String linkedStripeId = "";
            String profilePicture = "";
            String pdType = "";
            if (publicUserWithStripeIdNet != null) {
                email = publicUserWithStripeIdNet.getEmail();
                nickname = publicUserWithStripeIdNet.getNickname();
                linkedStripeId = publicUserWithStripeIdNet.getLinkedStripeId();
                if (publicUserWithStripeIdNet.getProfilePicture() != null) {
                    try {
                        profilePicture = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(publicUserWithStripeIdNet.getProfilePicture()), 8);
                    } catch (Exception e) {
                        pdLogger.logException(PdLogger.EVENT.IMAGE_CDN_LINK, e);
                        e.printStackTrace();

                    }
                }
                pdType = publicUserWithStripeIdNet.getPdType();
            }

            WithdrawalResponseWithStripeId response = new WithdrawalResponseWithStripeId(
                    withdrawal.getId(),
                    withdrawal.getPdUserId(),
                    withdrawal.getTrees(),
                    withdrawal.getLeafs(),
                    withdrawal.getStatus(),
                    withdrawal.getCreatedDate(),
                    withdrawal.getUpdatedDate(),
                    email,
                    nickname,
                    linkedStripeId,
                    profilePicture,
                    pdType

            );
            responseList.add(response);
        }
        return responseList;
    }

    public ResponseEntity<?> getAllWithDrawTransactionsForUserId() {
        try {
            String pdUserId = authHelper.getUserId();
            List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdOrderByCreatedDateDesc(pdUserId);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, withdrawalList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    public ResponseEntity<?> getPendingWithDrawTransactions() {
        try {
            String pdUserId = authHelper.getUserId();
            List<Withdrawal> withdrawalList = withdrawalRepository.findByStatus(WithdrawalStatus.PENDING);

            List<WithdrawalResponseWithStripeId> withdrawalResponseWithStripeIds = createWithDrawResponseWithStripeId(withdrawalList);

            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, withdrawalResponseWithStripeIds));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public ResponseEntity<?> getAllWithDrawTransactions(int page, int size, int sortOrder) {
        try {
            String pdUserId = authHelper.getUserId();

            // Define custom sorting comparator, To show all the data with status as complete first and then rest of the data
            Comparator<Withdrawal> customComparator = (w1, w2) -> {
                if (w1.getStatus() == WithdrawalStatus.COMPLETE && w2.getStatus() != WithdrawalStatus.COMPLETE) {
                    return -1; // w1 comes before w2
                } else if (w1.getStatus() != WithdrawalStatus.COMPLETE && w2.getStatus() == WithdrawalStatus.COMPLETE) {
                    return 1; // w2 comes before w1
                } else {
                    // If both have the same status or both are not "complete", sort by created date
                    return w1.getCreatedDate().compareTo(w2.getCreatedDate());
                }
            };

            // Define sorting criteria based on createdDate
            Sort sort = sortOrder == 0 ?
                    Sort.by(Sort.Direction.ASC, "createdDate") :
                    Sort.by(Sort.Direction.DESC, "createdDate");
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<Withdrawal> withdrawalPage = withdrawalRepository.findAll(pageRequest);

            //List<Withdrawal> withdrawalList = withdrawalPage.getContent();
            List<Withdrawal> withdrawalList = new ArrayList<>(withdrawalPage.getContent());
            withdrawalList.sort(customComparator);
            List<WithdrawalResponseWithStripeId> withdrawalResponseWithStripeIds = createWithDrawResponseWithStripeId(withdrawalList);

            // Create a PaginationInfo object with embedded response list
            PaginationInfoWithGenericList<WithdrawalResponseWithStripeId> paginationInfo = new PaginationInfoWithGenericList<>(
                    withdrawalPage.getNumber(),
                    withdrawalPage.getSize(),
                    withdrawalPage.getTotalElements(),
                    withdrawalPage.getTotalPages(),
                    withdrawalResponseWithStripeIds
            );
            return ResponseEntity.ok().body(new PaginationResponse(null, paginationInfo));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PaginationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    public WithdrawHistoryForPd getWithdrawHistoryTabForPdDetails(String pdUserId, LocalDate startDate, LocalDate endDate, int sortOrder, int page, int size) {
        WithdrawHistoryForPd withdrawHistoryForPd = new WithdrawHistoryForPd();

        String pdStripeId = withdrawalRepository.getPdStripeId(pdUserId);
        withdrawHistoryForPd.setPdStripeId(pdStripeId);

        // Define custom sorting comparator, To show all the data with status as pending first and then rest of the data
        Comparator<WithdrawHistoryForAdminDashboard> customComparator = (w1, w2) -> {
            if (w1.getStatus() == WithdrawalStatus.PENDING && w2.getStatus() != WithdrawalStatus.PENDING) {
                return -1; // w1 comes before w2
            } else if (w1.getStatus() != WithdrawalStatus.PENDING && w2.getStatus() == WithdrawalStatus.PENDING) {
                return 1; // w2 comes before w1
            } else {
                // If both have the same status or both are not "pending", sort by created date
                return 0;
            }
        };

        // Define sorting criteria based on createdDate
        Sort sort = sortOrder == 0 ?
                Sort.by(Sort.Direction.ASC, "created_date") :
                Sort.by(Sort.Direction.DESC, "created_date");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Pageable pageable = PageRequest.of(page, size, Sort.by("last_update_date").descending());
        Page<Object[]> whadPage = withdrawalRepository.findWithdrawalHistoryByPdId(pdUserId, startDate, endDate, pageRequest);

        List<WithdrawHistoryForAdminDashboard> wdList = new ArrayList<>();

        for (Object innerObject : whadPage.getContent()) {
            Object[] withdrawalHistory = (Object[]) innerObject;
            WithdrawHistoryForAdminDashboard wdobj = new WithdrawHistoryForAdminDashboard();

            double rate = Double.parseDouble(withdrawalHistory[3].toString());
            BigDecimal treesWithdrawn = new BigDecimal(withdrawalHistory[2].toString());
            BigDecimal result = treesWithdrawn.multiply(new BigDecimal(rate / 100));

            wdobj.setCreateDateTime(withdrawalHistory[0].toString());
            wdobj.setStatus(WithdrawalStatus.valueOf(withdrawalHistory[1].toString()));
            wdobj.setApplicationNumber(treesWithdrawn);
            wdobj.setRate(String.valueOf(rate) + "%");
            wdobj.setActualPayment(new BigDecimal(result.toString()).setScale(2, RoundingMode.HALF_UP));
            wdobj.setCompleteDate(withdrawalHistory[4].toString());
            wdList.add(wdobj);
        }
        wdList.sort(customComparator);
        withdrawHistoryForPd.setWithdrawHistoryForAdminDashboardList(new PageImpl<>(wdList, pageRequest, whadPage.getTotalElements()));
        return withdrawHistoryForPd;
    }

    public Optional<Withdrawal> findById(String id) {
        return withdrawalRepository.findById(id);
    }

}
