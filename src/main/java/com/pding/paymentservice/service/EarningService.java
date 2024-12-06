package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.exception.EarningNotFoundException;
import com.pding.paymentservice.exception.InsufficientLeafsException;
import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.UserObject;
import com.pding.paymentservice.payload.response.admin.TreeSummaryGridResult;
import com.pding.paymentservice.payload.response.custompagination.PaginationInfoWithGenericList;
import com.pding.paymentservice.payload.response.custompagination.PaginationResponse;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.service.AdminDashboard.TreeSummaryTabService;
import com.pding.paymentservice.util.FirebaseRealtimeDbHelper;
import com.pding.paymentservice.util.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class EarningService {

    @Autowired
    EarningRepository earningRepository;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    TreeSummaryTabService treeSummaryTabService;

    @Autowired
    FirebaseRealtimeDbHelper firebaseRealtimeDbHelper;

    @Transactional
    public void addTreesToEarning(String userId, BigDecimal trees) {
        log.info("Adding trees to earning for userId {}, trees {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(trees));
        Optional<Earning> earning = earningRepository.findByUserId(userId);

        Earning earningObj = null;
        if (earning.isPresent()) {
            earningObj = earning.get();

            BigDecimal updatedTreesEarned = earningObj.getTreesEarned().add(trees);

            earningObj.setTreesEarned(updatedTreesEarned);
            earningObj.setUpdatedDate(LocalDateTime.now());

            Integer updatedTotalTransactions = earningObj.getTotalTransactions() + 1;
            earningObj.setTotalTransactions(updatedTotalTransactions);
        } else {
            earningObj = new Earning(userId, trees, new BigDecimal(0));
        }
        earningRepository.save(earningObj);

        firebaseRealtimeDbHelper.updateEarningWalletBalanceInFirebase(userId, earningObj.getLeafsEarned(), earningObj.getTreesEarned());
//        pdLogger.logInfo("BUY_VIDEO", "Earning details recorded for UserUd: " + userId + ", trees : " + trees);
        log.info("Added trees to earning for userId {}, trees {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(trees));
    }


    @Transactional
    public void addLeafsToEarning(String userId, BigDecimal leafs) {
        log.info("Adding leafs to earning for userId {}, leafs {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(leafs));
        Optional<Earning> earning = earningRepository.findByUserId(userId);

        Earning earningObj = null;
        if (earning.isPresent()) {
            earningObj = earning.get();

            BigDecimal updatedLeafsEarned = earningObj.getLeafsEarned().add(leafs);

            earningObj.setLeafsEarned(updatedLeafsEarned);
            earningObj.setUpdatedDate(LocalDateTime.now());

            Integer updatedTotalTransactions = earningObj.getTotalTransactions() + 1;
            earningObj.setTotalTransactions(updatedTotalTransactions);
        } else {
            earningObj = new Earning(userId, new BigDecimal(0), leafs);
        }
        earningRepository.save(earningObj);

        firebaseRealtimeDbHelper.updateEarningWalletBalanceInFirebase(userId, earningObj.getLeafsEarned(), earningObj.getTreesEarned());
        log.info("Added leafs to earning for userId {}, leafs {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(leafs));
    }


    public Optional<Earning> fetchEarningForUserId(String userId) {
        Optional<Earning> earning = earningRepository.findByUserId(userId);
        if (!earning.isPresent()) {
            Earning earningObj = new Earning();
            earningObj.setUserId(userId);
            earningObj.setTreesEarned(new BigDecimal(0));
            earningObj.setLeafsEarned(new BigDecimal(0));
            earningObj.setTotalTransactions(0);

            earningRepository.save(earningObj);

            firebaseRealtimeDbHelper.updateEarningWalletBalanceInFirebase(userId, earningObj.getLeafsEarned(), earningObj.getTreesEarned());
            return earningRepository.findByUserId(userId);
        }

        firebaseRealtimeDbHelper.updateEarningWalletBalanceInFirebase(userId, earning.get().getLeafsEarned(), earning.get().getTreesEarned());
        return earning;
    }


    @Transactional
    public void deductTreesFromEarning(String userId, BigDecimal treesToDeduct) {
        Optional<Earning> earning = fetchEarningForUserId(userId);

        if (earning.isPresent()) {
            Earning earningObj = earning.get();
            BigDecimal currentTrees = earningObj.getTreesEarned();

            if (treesToDeduct.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal newTreesBalance = currentTrees.subtract(treesToDeduct);
                if (newTreesBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    earningObj.setTreesEarned(newTreesBalance);
                    earningRepository.save(earningObj);
                    firebaseRealtimeDbHelper.updateEarningWalletBalanceInFirebase(userId, earningObj.getLeafsEarned(), earningObj.getTreesEarned());
                } else {
                    log.error("Insufficient trees. Cannot perform transaction.");
                    throw new InsufficientTreesException("Insufficient trees. Cannot perform transaction.");
                }
            } else {
                log.error("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
                throw new InvalidAmountException("Invalid amount(Trees) to deduct. Amount(Trees) must be greater than or equal to zero.");
            }
        } else {
            log.error("No Earning info present for userId " + userId);
            log.error("Creating Earning info for for userId " + userId);
            throw new EarningNotFoundException("No Earnings found for userID " + userId);
        }
    }

    @Transactional
    public void deductLeafsFromEarning(String userId, BigDecimal leafsToDeduct) {
        Optional<Earning> earning = fetchEarningForUserId(userId);

        if (earning.isPresent()) {
            Earning earningObj = earning.get();
            BigDecimal currentTrees = earningObj.getLeafsEarned();

            if (leafsToDeduct.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal newLeafsBalance = currentTrees.subtract(leafsToDeduct);
                if (newLeafsBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    earningObj.setLeafsEarned(newLeafsBalance);
                    earningRepository.save(earningObj);
                    firebaseRealtimeDbHelper.updateEarningWalletBalanceInFirebase(userId, earningObj.getLeafsEarned(), earningObj.getTreesEarned());
                } else {
                    log.error("Insufficient leafs. Cannot perform transaction.");
                    throw new InsufficientLeafsException("Insufficient Leafs. Cannot perform transaction.");
                }
            } else {
                log.error("Invalid amount(Leafs) to deduct. Amount(Leafs) must be greater than or equal to zero.");
                throw new InvalidAmountException("Invalid amount(Leafs) to deduct. Amount(Leafs) must be greater than or equal to zero.");
            }
        } else {
            log.error("No Earning info present for userId " + userId);
            log.error("Creating Earning info for for userId " + userId);
            throw new EarningNotFoundException("No Earnings found for userID " + userId);
        }
    }

    void deductTreesAndLeafs(String userId, BigDecimal treesToDeduct, BigDecimal leafsToDeduct) {
        deductTreesFromEarning(userId, treesToDeduct);
        deductLeafsFromEarning(userId, leafsToDeduct);
    }

    void addTreesAndLeafsToEarning(String userId, BigDecimal treesToAdd, BigDecimal leafsToAdd) {
        addTreesToEarning(userId, treesToAdd);
        addLeafsToEarning(userId, leafsToAdd);
    }


    public ResponseEntity<?> getTopEarners(int page, int size) {
        try {
            TreeSummaryGridResult res = treeSummaryTabService.getTreesSummaryForAllUsers(null, null, null, page, size);
            Page<UserObject> earnersPage = res.getUserObjects();
            PaginationInfoWithGenericList<UserObject> paginationInfo = new PaginationInfoWithGenericList<>(
                    earnersPage.getNumber(),
                    earnersPage.getSize(),
                    earnersPage.getTotalElements(),
                    earnersPage.getTotalPages(),
                    earnersPage.getContent()
            );
            return ResponseEntity.ok().body(new PaginationResponse(null, paginationInfo));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PaginationResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    public BigDecimal sumOfAllTreesEarned() {
        return earningRepository.sumOfAllTreesEarned();
    }
}
