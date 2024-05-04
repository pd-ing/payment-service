package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtherServicesTablesNativeQueryRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT id, COALESCE(email, '') AS email, COALESCE(pd_type, '') AS pdType, COALESCE(nickname, '') AS nickname, COALESCE(linked_stripe_id, '') AS linkedStripeId FROM users WHERE id = :userId", nativeQuery = true)
    List<Object[]> findUserInfoByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM referrals WHERE referred_pd_user_id = :referredPdUserId", nativeQuery = true)
    List<Object[]> findReferralDetailsByReferredPdUserId(@Param("referredPdUserId") String referredPdUserId);

    @Query(value = "SELECT COALESCE(u.nickname, '') as nickname, " +
            "COALESCE(u.pd_type, '') as pd_type, " +
            "COALESCE(u.created_date, '') as created_date, " +
            "COALESCE(e.trees_earned, '') as trees_earned, " +
            "COALESCE(e.leafs_earned, '') as leafs_earned, " +
            "COALESCE(r.referred_pd_user_id, '') as referred_pd_user_id, " +
            "COALESCE((SELECT w.created_date FROM withdrawals w " +
            "WHERE w.pd_user_id = u.id ORDER BY w.created_date DESC LIMIT 1), '') as last_exchange_date " +
            "FROM referrals r " +
            "JOIN users u ON r.referred_pd_user_id = u.id " +
            "JOIN earning e ON e.user_id = r.referred_pd_user_id " +
            "WHERE r.referrer_pd_user_id = :referrerPdUserId",
            countQuery = "SELECT count(*) " +
                    "FROM referrals r " +
                    "JOIN users u ON r.referred_pd_user_id = u.id " +
                    "JOIN earning e ON e.user_id = r.referred_pd_user_id " +
                    "WHERE r.referrer_pd_user_id = :referrerPdUserId",
            nativeQuery = true)
    Page<Object[]> getDetailsOfAllTheReferredPd(String referrerPdUserId, Pageable pageable);
}
