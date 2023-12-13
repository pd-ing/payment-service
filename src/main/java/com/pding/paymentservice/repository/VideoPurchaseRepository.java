package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VideoPurchaseRepository extends JpaRepository<VideoPurchase, String> {

    //VideoPurchase save(VideoPurchase transaction);

    List<VideoPurchase> getVideoPurchaseByUserId(String userId);


    @Query("SELECT SUM(vt.treesConsumed) FROM VideoPurchase vt WHERE vt.videoOwnerUserId = :videoOwnerUserId")
    BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserId);

    // Query method to find records by userID and videoID
    List<VideoPurchase> findByUserIdAndVideoId(String userId, String videoId);


    @Query("SELECT COALESCE(SUM(vp.treesConsumed), 0) FROM VideoPurchase vp WHERE vp.videoId = :videoId")
    BigDecimal getTotalTreesEarnedForVideoId(@Param("videoId") String videoId);

    @Query("SELECT COUNT(vp) FROM VideoPurchase vp WHERE vp.videoId = :videoId")
    long getSalesCountForVideoId(@Param("videoId") String videoId);
}
