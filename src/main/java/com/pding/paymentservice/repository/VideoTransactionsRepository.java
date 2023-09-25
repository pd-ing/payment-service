package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoTransactionsRepository extends JpaRepository<VideoTransactions, Long> {

    VideoTransactions save(VideoTransactions transaction);

    List<VideoTransactions> getVideoTransactionsByUserID(long userID);

    List<VideoTransactions> getVideoTransactionsByContentID(long contentID);
}
