package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.CallDetails;
import com.pding.paymentservice.models.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CallRepository extends JpaRepository<CallDetails, String> {
    List<CallDetails> findByCallTypeAndPdUserId(TransactionType callType, String userId);

    List<CallDetails> findByUserId(String userId);


    List<CallDetails> findByPdUserId(String pdUserId);

    @Query("SELECT c.userId, SUM(c.leafsTransacted) as leafsTransacted " +
            "FROM CallDetails c " +
            "WHERE c.pdUserId = :pdUserId " +
            "GROUP BY c.userId " +
            "ORDER BY leafsTransacted DESC " +
            "LIMIT :limit")
    List<Object[]> findTopCallerUserByPdUserID(@Param("pdUserId") String pdUserId, @Param("limit") Long limit);

    //    @Query("SELECT cd.userId, SUM(cd.leafsTransacted) as totalLeafsTransacted " +
//            "FROM CallDetails cd " +
//            "WHERE cd.userId = :userId " +
//            "GROUP BY cd.userId " +
//            "ORDER BY totalLeafsTransacted DESC" +
//            "LIMIT :limit")
    @Query("SELECT c.userId, SUM(c.leafsTransacted) as leafsTransacted " +
            "FROM CallDetails c " +
            "WHERE c.userId = :userId " +
            "GROUP BY c.userId " +
            "ORDER BY leafsTransacted DESC " +
            "LIMIT :limit")
    List<Object[]> findTopCallerUsers(@Param("userId") String userId, @Param("limit") Long limit);
}


