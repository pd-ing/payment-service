package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.CallPurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CallPurchaseRepository extends JpaRepository<CallPurchase, String> {
    List<CallPurchase> findByCallTypeAndPdUserId(TransactionType callType, String userId);

    List<CallPurchase> findByUserId(String userId);


    List<CallPurchase> findByPdUserId(String pdUserId);

    @Query("SELECT c.userId, SUM(c.leafsTransacted) as leafsTransacted " +
            "FROM CallPurchase c " +
            "WHERE c.pdUserId = :pdUserId " +
            "GROUP BY c.userId " +
            "ORDER BY leafsTransacted DESC " +
            "LIMIT :limit")
    List<Object[]> findTopCallerUserByPdUserID(@Param("pdUserId") String pdUserId, @Param("limit") Long limit);

    //    @Query("SELECT cd.userId, SUM(cd.leafsTransacted) as totalLeafsTransacted " +
//            "FROM CallPurchase cd " +
//            "WHERE cd.userId = :userId " +
//            "GROUP BY cd.userId " +
//            "ORDER BY totalLeafsTransacted DESC" +
//            "LIMIT :limit")
    @Query("SELECT c.userId, SUM(c.leafsTransacted) as leafsTransacted " +
            "FROM CallPurchase c " +
            "WHERE c.userId = :userId " +
            "GROUP BY c.userId " +
            "ORDER BY leafsTransacted DESC " +
            "LIMIT :limit")
    List<Object[]> findTopCallerUsers(@Param("userId") String userId, @Param("limit") Long limit);

    @Query("SELECT cp.userId, cp.pdUserId, SUM(cp.leafsTransacted) " +
            "FROM CallPurchase cp " +
            "WHERE cp.callId = :callId " +
            "GROUP BY cp.userId, cp.pdUserId")
    List<Object[]> findUserIdPdUserIdAndSumLeafsTransactedByCallId(@Param("callId") String callId);
}


