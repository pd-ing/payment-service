package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtherServicesTablesNativeQueryRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT id, email, pd_type AS pdType, nickname, linked_stripe_id FROM users WHERE id = :userId", nativeQuery = true)
    List<Object[]> findUserInfoByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM referrals WHERE referred_pd_user_id = :referredPdUserId", nativeQuery = true)
    List<Object[]> findReferralDetailsByReferredPdUserId(@Param("referredPdUserId") String referredPdUserId);
}
