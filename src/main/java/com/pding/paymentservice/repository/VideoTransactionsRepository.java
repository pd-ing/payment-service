package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoTransactionsRepository extends JpaRepository<VideoTransactions, Long> {

    VideoTransactions save(VideoTransactions transaction);

    List<VideoTransactions> getVideoTransactionsByUserID(long userID);


    @Query("SELECT SUM(vt.treesConsumed) FROM VideoTransactions vt WHERE vt.videoOwnerUserID = :videoOwnerUserID")
    BigDecimal getTotalTreesEarnedByVideoOwner(Long videoOwnerUserID);

    // Query method to find records by userID and videoID
    List<VideoTransactions> findByUserIDAndVideoID(Long userID, String videoID);
}
