package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VideoPurchaseRepository extends JpaRepository<VideoPurchase, Long> {

    //VideoPurchase save(VideoPurchase transaction);

    List<VideoPurchase> getVideoPurchaseByUserId(String userId);


    @Query("SELECT SUM(vt.treesConsumed) FROM VideoPurchase vt WHERE vt.videoOwnerUserId = :videoOwnerUserId")
    BigDecimal getTotalTreesEarnedByVideoOwner(String videoOwnerUserId);

    // Query method to find records by userID and videoID
    List<VideoPurchase> findByUserIdAndVideoId(String userId, String videoId);
}
